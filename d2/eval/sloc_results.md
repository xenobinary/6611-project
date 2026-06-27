## Physical SLOC

```bash
cloc . --exclude-dir=resources --by-file
      26 text files.
      26 unique files.                              
       1 file ignored.
```
```plaintext
github.com/AlDanial/cloc v 2.06  T=0.06 s (453.2 files/s, 63165.5 lines/s)
----------------------------------------------------------------------------------------------
File                                                       blank        comment           code
----------------------------------------------------------------------------------------------
./views/ClientDashboardPanel.java                             42             47            427
./views/AdminDashboardPanel.java                              25             24            240
./models/DatabaseManager.java                                 20             89            224
./views/LoginPanel.java                                       26             24            167
./controllers/TransactionController.java                       8             72            145
./views/LanguageSelectorPanel.java                            21             35            132
./views/NumpadPanel.java                                      16             47            127
./views/TechnicianPanel.java                                  14             13            106
./views/BaseViewPanel.java                                    18             58            101
./views/MainFrame.java                                        18             27             99
./controllers/I18nController.java                             13             62             81
./controllers/AuthenticationController.java                    8             44             77
./models/ExchangeRateManager.java                              8             47             69
./models/TransactionRecord.java                                5             46             59
./models/CashBox.java                                         12             63             53
./models/User.java                                             9             59             52
./models/Account.java                                          8             50             39
./models/BankClient.java                                       6             28             27
./models/SystemAdmin.java                                      3             16             18
./lsloc.sh                                                    13             15             17
./exceptions/InsufficientFundException.java                    3             22             15
./models/Technician.java                                       2             11             13
./exceptions/AccountLockedException.java                       3             20             12
./exceptions/InvalidAmountException.java                       3             21             12
./views/NumpadListener.java                                    5             15              8
./views/Router.java                                            3             31              6
----------------------------------------------------------------------------------------------
SUM:                                                         312            986           2326
----------------------------------------------------------------------------------------------
```

## Logical SLOC

```bash
#!/bin/bash

trap 'find . -type f -name "*.java.clean" -delete 2>/dev/null' EXIT

echo "Analyzing Java files..."

find . -type f -name "*.java" -not -path "*/test/*" -not -path "*/tests/*" \
    -exec cloc --quiet --strip-comments=clean --original-dir {} + > /dev/null 2>&1

printf "\n%-60s | %-12s\n" "File Path" "Logical SLOC"
echo "-------------------------------------------------------------+--------------"

total_sloc=0

while IFS= read -r -d '' clean_file; do

    count=$(grep -E -o ";|\bif\s*\(|\bfor\s*\(|\bwhile\s*\(" "$clean_file" | wc -l)

    display_name="${clean_file%.clean}"
    display_name="${display_name#./}"

    printf "%-60.60s | %12d\n" "$display_name" "$count"

    total_sloc=$((total_sloc + count))

done < <(find . -type f -name "*.java.clean" -print0)

echo "-------------------------------------------------------------+--------------"
printf "%-60s | %12d\n\n" "TOTAL" "$total_sloc"

```

```plaintext
Analyzing Java files...

File Path                                                    | Logical SLOC
-------------------------------------------------------------+--------------
exceptions/InsufficientFundException.java                    |            7
exceptions/InvalidAmountException.java                       |            6
exceptions/AccountLockedException.java                       |            6
models/Account.java                                          |           21
models/User.java                                             |           30
models/SystemAdmin.java                                      |            5
models/BankClient.java                                       |           12
models/ExchangeRateManager.java                              |           40
models/CashBox.java                                          |           24
models/DatabaseManager.java                                  |          136
models/TransactionRecord.java                                |           28
models/Technician.java                                       |            3
controllers/TransactionController.java                       |           77
controllers/I18nController.java                              |           44
controllers/AuthenticationController.java                    |           38
views/MainFrame.java                                         |           69
views/ClientDashboardPanel.java                              |          356
views/NumpadListener.java                                    |            6
views/BaseViewPanel.java                                     |           89
views/NumpadPanel.java                                       |          106
views/Router.java                                            |            4
views/TechnicianPanel.java                                   |           67
views/LanguageSelectorPanel.java                             |          100
views/AdminDashboardPanel.java                               |          189
views/LoginPanel.java                                        |          126
-------------------------------------------------------------+--------------
TOTAL                                                        |         1589
```