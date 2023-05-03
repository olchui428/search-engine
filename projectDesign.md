# COMP4321 Project Design

## Full Stack Search Engine

This project implements a front-to-back system that crawls webpages and allows searching upon the crawled sites.

* Frontend: ReactJS styled with antd
* Backend: Java with JDBM database

### Frontend Capabilities
....

## Algorithms

### Phrase Search

Phrase search is enabled by storing the position of each word into the *inverted_index* and the `forwardIndex` of webpages. When a query with phrase is requested, the Search Engine will loop through each word in the phrase and maps the webpage ID to the array of positions in which the word appears. For a given webpage, if the positions of the previous word is in front of any of the positions of the current word, then a phrase is found.

### Weights - TFxIDF

A match between the query and a webpage title are favoured over a match with the body. Hence a `TITLE_BONUS` of value larger than 1 is multiplied to the title weight.

#### Tokenized query

* Match in webpage title: `weight = tf * idf * TITLE_BONUS`
* Match in webpage body: `weight = tf * idf / maxTf`

`tf`: term frequency of a given term in a given webpage
`idf`: inverse document frequency of a given term
`maxTf`: normalization factor. term frequency of the most frequent term in a given webpage

#### Phrase query

* Phrase match in webpage title: `weight = pf * idfP * TITLE_BONUS`
* Phrase match in webpage body: `weight = pf * idfP / maxPf`

`pf`: phrase frequency of a given phrase in a given webpage
`idfP`: inverse document frequency of a given phrase
`maxPf`: normalization factor. phrase frequency of the most frequent phrase in a given webpage

### Similarity Measure – Cosine Similarity

Cosine similarity is used to calculate the similarity between a query and a document (webpage).

$$ CosSim(D_i , Q) = {D_i \cdot Q \over \mid D \mid \mid Q \mid }$$

## Database Design

Using the JDBM library, data is stored as key-value pairs in the database.
The following hashmaps are used to store data in this project:

* *pages*: stores the information of all fetched webpages. Forward index is also supported in each page object.
* *inverted_index_body*: stores the inverted index of body content extracted from webpages.
* *inverted_index_title*: stores the inverted index of body content extracted from webpages.
* *term_to_id*: stores the mapping from a word (term) to its wordId, which is consistent with the inverted index.

### Design principle

Striving a balance between minimizing storage and efficiency by designing minimal database tables while ensuring speedy response can be
delivered upon a query prompt.
The database is designed to achieve simplicity that can help reduce the risk of errors
and inconsistencies in the data. Storage of data can also be optimized with a simplistic database.
Despite the goal to achieve minimalistic design, duplicative data such as forward index and *term_to_id* are however implemented
to deliver prompt response by avoiding the excessive data iterations. Forward index helps accelerate document (page)
deletion in the inverted index, whereas *term_to_id* table is created to find the id of a word (term)
quickly.

### Pages Database

#### Structure
A hashmap (htree) that maps pageId to a Page object.

#### Page Object
Stores information of a webpage

Attributes
* `page_id`: (String)
* `url`: (URL)
* `lastModificationDate`: (LocalDateTime)
* `pageSize`: (int)
* `title`: (String)
* `body`: (ArrayList\<String>)
* `childLinks`: (HashSet\<URL>)
* `parentLinks`: (HashSet\<URL>)
* `forwardIndex`: (Hashtable\<String, Integer>) maps word to its term frequency (tf) in the page. to support forward index.

### Inverted Index Database (for both title and body)

#### Structure
A hashmap (htree) that maps wordId to a Term object.

#### Term Object
Stores information of a word (term)

Attributes
* `wordId`: (String)
* `word`: (String)
* `df`: (int) document frequency
* `postingList`: (ArrayList\<Posting>)

#### Posting Object
Stores information of the occurrence of a word (term) in different documents (pages)

Attributes
* `pageId`: (String)
* `tf`: (int) term frequency

### Term to ID Database

#### Structure
A hashmap (htree) that maps word (term) to its wordId, which is consistent with inverted indexes.



## Worker Classes

### Spider Class
Scrapes information and hyperlinks from a given webpage url.

Public Methods
* `Spider(DatabaseManager recman, String url, int numPages)`: takes a DatabaseManager that
  handles interactions with the *pages* database, a starting URL and the limit to the number of pages to be fetched.
* `fetch()`: conducts a breadth-first search to extract hyperlinks from a webpage.
  It first checks whether a particular webpage should be fetched,
  then proceeds to extracting all required information from each webpage and stores
  them in a Page object. The Page object is then updated in the *pages* database.

Private Methods (helper functions)
* `exractPageSize(URL url)`
* `extractPageDate(URL url)`
* `extractChildLinks(URL url)`
* `extractTitle(URL url)`
* `extractBody(URL url)`
* `shouldFetch(URL url)`

### Indexer Class
Processes webpages from the *pages* database to extract keywords and updates inverted index files.
Forward indexes for each webpage in *pages* is also updated.

Public Methods
* `Indexer(DatabaseManager recmanInvBody)`: takes a DatabaseManager that
  handles interactions with the *inverted_index* databases. Within the constructor, another
  DatabaseManager that links to the *pages* database is created to load pages that were fetched beforehand.
* `process()`: for each webpage from the *pages* database, stopwords are removed and tokens from title and
  body are reduced to their stems. Tokens from titles and bodies of webpages are subsequently indexed to
  inverted index files. The inverted indexes are then stored in the database.
* `removeStopWords(ArrayList<String> body, Collection<String> stopWords)`
* `stem(ArrayList<String> body)`

Private Methods (helper functions)
* `fetchStopWords(String stopwordsFilepath)`: loads stopwords from a text file given its filepath.
* `indexing(ArrayList<String> body, String pageId)`: counts occurrences of each
  word (term) to update `forwardIndex` in each given webpage. It then updates a class variable `invertedFile`
  regarding each term information. It also maintains a mapping from word (term) to its wordId. Note that database has not been updated yet.
* `updateInvertedFile()`: updates *inverted_index* databases.

### SearchEngine Class
...

Public Methods
* `start()`: ...

Private Methods (helper functions)
* `...`: ...