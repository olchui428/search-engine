# COMP4321 README

The project is developed in Java on IntelliJ.

### Dependencies
On IntelliJ, do to File > Project Structure > Project Settings > Libraries > add the 
directory of this project's *utils* folder.

* `utils/htmlparser.jar`
* `utils/jdbm-1.0.jar`
* `utils/Porter.java`
* `utils/stopwords.txt`

### Running the Project

In `TestClass.java`, run

* `testMain()`: to crawl data from webpages given a starting URL, and indexes all fetched webpages.
* `testOutputPagesToTxt()`: to output entries from *pages* database to `./output/spider_results.txt` file.
* `testOutputInvIndexToTxt()`: to output entries from *inverted_index* database to `./output/indexer_body_results.txt` file.
