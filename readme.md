# COMP4321 README

## Installation and Getting Started

The backend of project is developed in Java on IntelliJ.

### Backend

#### Dependencies

On IntelliJ, do to File > Project Structure > Project Settings > Libraries > add the
directory of this project's *utils* folder.

* `utils/htmlparser.jar`
* `utils/jdbm-1.0.jar`
* `utils/Porter.java`
* `utils/stopwords.txt`

#### Running

In `Server.java`, run

* `main()`: to start the server at port 8000 and accepts HTTP requests from the frontend.

### Frontend

* `cd client`: go to *client* directory
* `npm i`: install dependencies
* `npm start`: start frontend at port 3000.
