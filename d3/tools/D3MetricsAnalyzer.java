import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.JavacTask;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import com.sun.source.util.Trees;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.WildcardType;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

/**
 * Computes the source-level inputs required for D3 CF and Henderson-Sellers
 * LCOM* measurements.
 *
 * <p>Measurement conventions:
 * <ul>
 *   <li>The analysis universe contains every project class and interface.</li>
 *   <li>CF is directed outgoing project-type coupling divided by N - 1.</li>
 *   <li>Inheritance is excluded from CF, following the MOOD coupling-factor
 *       definition.</li>
 *   <li>LCOM* uses declared instance fields and declared instance methods.</li>
 *   <li>Constructors, static methods, abstract methods, and inherited members
 *       are excluded from LCOM*.</li>
 *   <li>LCOM* is not applicable when a type has no instance fields or fewer
 *       than two eligible methods.</li>
 * </ul>
 */
public final class D3MetricsAnalyzer {
    private D3MetricsAnalyzer() {
    }

    private static final class TypeInfo {
        final TypeElement element;
        final TreePath path;
        final String name;
        final String kind;
        final Set<Element> instanceFields = new HashSet<>();
        final List<TreePath> instanceMethods = new ArrayList<>();
        final Set<TypeElement> inheritedTypes = new HashSet<>();
        final Set<TypeElement> coupledTypes = new HashSet<>();
        int fieldAccesses;
        Double lcomStar;

        TypeInfo(TypeElement element, TreePath path, String kind) {
            this.element = element;
            this.path = path;
            this.name = element.getQualifiedName().toString();
            this.kind = kind;
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.err.println(
                    "Usage: java D3MetricsAnalyzer <source-directory> <output-csv>");
            System.exit(2);
        }

        Path sourceDir = Paths.get(args[0]).toAbsolutePath().normalize();
        Path outputCsv = Paths.get(args[1]).toAbsolutePath().normalize();
        List<Path> sourceFiles;
        try (var paths = Files.walk(sourceDir)) {
            sourceFiles = paths
                    .filter(path -> path.toString().endsWith(".java"))
                    .sorted()
                    .collect(Collectors.toList());
        }

        if (sourceFiles.isEmpty()) {
            throw new IllegalArgumentException("No Java source files found: " + sourceDir);
        }

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            throw new IllegalStateException("A JDK is required; no system compiler found.");
        }

        try (StandardJavaFileManager fileManager =
                     compiler.getStandardFileManager(null, Locale.ROOT, StandardCharsets.UTF_8)) {
            Iterable<? extends JavaFileObject> units =
                    fileManager.getJavaFileObjectsFromPaths(sourceFiles);
            JavacTask task = (JavacTask) compiler.getTask(
                    null,
                    fileManager,
                    diagnostic -> System.err.println(diagnostic.toString()),
                    List.of("-proc:none"),
                    null,
                    units);

            Iterable<? extends CompilationUnitTree> parsed = task.parse();
            task.analyze();

            Trees trees = Trees.instance(task);
            Map<TypeElement, TypeInfo> types = collectTypes(parsed, trees);
            analyzeMembers(types, trees);
            analyzeCoupling(types, trees);
            writeCsv(types, outputCsv);
        }
    }

    private static Map<TypeElement, TypeInfo> collectTypes(
            Iterable<? extends CompilationUnitTree> units,
            Trees trees) {
        Map<TypeElement, TypeInfo> types = new LinkedHashMap<>();

        for (CompilationUnitTree unit : units) {
            TreePath unitPath = new TreePath(unit);
            for (Tree declaration : unit.getTypeDecls()) {
                if (declaration instanceof ClassTree classTree) {
                    TreePath typePath = new TreePath(unitPath, declaration);
                    Element element = trees.getElement(typePath);
                    if (element instanceof TypeElement typeElement) {
                        String kind = classTree.getKind() == Tree.Kind.INTERFACE
                                ? "interface"
                                : "class";
                        types.put(typeElement, new TypeInfo(typeElement, typePath, kind));
                    }
                }
            }
        }
        return types;
    }

    private static void analyzeMembers(Map<TypeElement, TypeInfo> types, Trees trees) {
        for (TypeInfo info : types.values()) {
            ClassTree classTree = (ClassTree) info.path.getLeaf();

            collectInheritedTypes(classTree.getExtendsClause(), info, types, trees);
            for (Tree implemented : classTree.getImplementsClause()) {
                collectInheritedTypes(implemented, info, types, trees);
            }

            for (Tree member : classTree.getMembers()) {
                TreePath memberPath = new TreePath(info.path, member);
                Element element = trees.getElement(memberPath);

                if (member instanceof VariableTree
                        && element instanceof VariableElement variable
                        && variable.getKind() == ElementKind.FIELD
                        && !variable.getModifiers().contains(Modifier.STATIC)) {
                    info.instanceFields.add(variable);
                }

                if (member instanceof MethodTree method
                        && method.getBody() != null
                        && element != null
                        && element.getKind() == ElementKind.METHOD
                        && !element.getModifiers().contains(Modifier.STATIC)) {
                    info.instanceMethods.add(memberPath);
                }
            }

            computeLcomStar(info, trees);
        }
    }

    private static void collectInheritedTypes(
            Tree tree,
            TypeInfo info,
            Map<TypeElement, TypeInfo> projectTypes,
            Trees trees) {
        if (tree == null) {
            return;
        }
        TreePath path = new TreePath(info.path, tree);
        Element element = trees.getElement(path);
        if (element instanceof TypeElement typeElement && projectTypes.containsKey(typeElement)) {
            info.inheritedTypes.add(typeElement);
        }
    }

    private static void computeLcomStar(TypeInfo info, Trees trees) {
        int methodCount = info.instanceMethods.size();
        int fieldCount = info.instanceFields.size();
        if (methodCount < 2 || fieldCount == 0) {
            info.lcomStar = null;
            return;
        }

        Map<Element, Integer> accessCounts = new HashMap<>();
        for (Element field : info.instanceFields) {
            accessCounts.put(field, 0);
        }

        for (TreePath methodPath : info.instanceMethods) {
            Set<Element> fieldsAccessedByMethod = new HashSet<>();
            new TreePathScanner<Void, Void>() {
                private void recordCurrentElement() {
                    Element element = trees.getElement(getCurrentPath());
                    if (info.instanceFields.contains(element)) {
                        fieldsAccessedByMethod.add(element);
                    }
                }

                @Override
                public Void visitIdentifier(IdentifierTree node, Void unused) {
                    recordCurrentElement();
                    return super.visitIdentifier(node, unused);
                }

                @Override
                public Void visitMemberSelect(MemberSelectTree node, Void unused) {
                    recordCurrentElement();
                    return super.visitMemberSelect(node, unused);
                }
            }.scan(methodPath, null);

            for (Element field : fieldsAccessedByMethod) {
                accessCounts.compute(field, (ignored, count) -> count + 1);
            }
        }

        int totalFieldAccesses = accessCounts.values().stream()
                .mapToInt(Integer::intValue)
                .sum();
        info.fieldAccesses = totalFieldAccesses;
        double averageMethodsPerField = (double) totalFieldAccesses / fieldCount;
        info.lcomStar =
                (methodCount - averageMethodsPerField) / (methodCount - 1.0);
    }

    private static void analyzeCoupling(Map<TypeElement, TypeInfo> types, Trees trees) {
        Set<TypeElement> projectTypes = types.keySet();

        for (TypeInfo info : types.values()) {
            new TreePathScanner<Void, Void>() {
                private void collectCurrentElement() {
                    Element element = trees.getElement(getCurrentPath());
                    if (element == null) {
                        return;
                    }
                    collectType(element.asType(), info.coupledTypes, projectTypes);
                    Element owner = element.getEnclosingElement();
                    if (owner instanceof TypeElement typeElement
                            && projectTypes.contains(typeElement)) {
                        info.coupledTypes.add(typeElement);
                    }
                }

                @Override
                public Void visitIdentifier(IdentifierTree node, Void unused) {
                    collectCurrentElement();
                    return super.visitIdentifier(node, unused);
                }

                @Override
                public Void visitMemberSelect(MemberSelectTree node, Void unused) {
                    collectCurrentElement();
                    return super.visitMemberSelect(node, unused);
                }
            }.scan(info.path, null);

            info.coupledTypes.remove(info.element);
            info.coupledTypes.removeAll(info.inheritedTypes);
        }
    }

    private static void collectType(
            TypeMirror mirror,
            Set<TypeElement> destination,
            Set<TypeElement> projectTypes) {
        if (mirror == null || mirror.getKind() == TypeKind.NONE) {
            return;
        }

        switch (mirror.getKind()) {
            case ARRAY -> collectType(
                    ((ArrayType) mirror).getComponentType(), destination, projectTypes);
            case DECLARED -> {
                DeclaredType declared = (DeclaredType) mirror;
                Element element = declared.asElement();
                if (element instanceof TypeElement typeElement
                        && projectTypes.contains(typeElement)) {
                    destination.add(typeElement);
                }
                for (TypeMirror argument : declared.getTypeArguments()) {
                    collectType(argument, destination, projectTypes);
                }
            }
            case TYPEVAR -> {
                TypeVariable variable = (TypeVariable) mirror;
                collectType(variable.getUpperBound(), destination, projectTypes);
                collectType(variable.getLowerBound(), destination, projectTypes);
            }
            case WILDCARD -> {
                WildcardType wildcard = (WildcardType) mirror;
                collectType(wildcard.getExtendsBound(), destination, projectTypes);
                collectType(wildcard.getSuperBound(), destination, projectTypes);
            }
            default -> {
                // Primitive, void, executable, package, and error types add no coupling.
            }
        }
    }

    private static void writeCsv(Map<TypeElement, TypeInfo> types, Path outputCsv)
            throws IOException {
        Files.createDirectories(outputCsv.getParent());
        List<TypeInfo> rows = new ArrayList<>(types.values());
        rows.sort(Comparator.comparing(info -> info.name));
        int typeCount = rows.size();

        try (BufferedWriter writer =
                     Files.newBufferedWriter(outputCsv, StandardCharsets.UTF_8)) {
            writer.write(
                    "type,kind,instance_fields,instance_methods,field_accesses,"
                            + "lcom_star,coupled_types,coupling_count,cf");
            writer.newLine();

            for (TypeInfo info : rows) {
                String coupled = info.coupledTypes.stream()
                        .map(type -> type.getQualifiedName().toString())
                        .sorted()
                        .collect(Collectors.joining(";"));
                double cf = typeCount <= 1
                        ? 0.0
                        : (double) info.coupledTypes.size() / (typeCount - 1);
                String lcom = info.lcomStar == null
                        ? "N/A"
                        : String.format(Locale.ROOT, "%.6f", info.lcomStar);

                writer.write(String.join(",",
                        csv(info.name),
                        info.kind,
                        Integer.toString(info.instanceFields.size()),
                        Integer.toString(info.instanceMethods.size()),
                        Integer.toString(info.fieldAccesses),
                        lcom,
                        csv(coupled),
                        Integer.toString(info.coupledTypes.size()),
                        String.format(Locale.ROOT, "%.6f", cf)));
                writer.newLine();
            }
        }

        int directedCouplings = rows.stream()
                .mapToInt(info -> info.coupledTypes.size())
                .sum();
        double systemCf = typeCount <= 1
                ? 0.0
                : (double) directedCouplings / (typeCount * (typeCount - 1));
        System.out.printf(
                Locale.ROOT,
                "Analyzed %d project types; directed couplings=%d; system CF=%.6f%n",
                typeCount,
                directedCouplings,
                systemCf);
        System.out.println("Wrote " + outputCsv);
    }

    private static String csv(String value) {
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }
}
