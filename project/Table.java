package project;

import java.util.ArrayList;

enum TableName {
    INVERTED_INDEX_TITLE,
    INVERTED_INDEX_BODY,
    TERM_TO_ID,
    PAGES,
    URL_to_ID
}

public class Table {

    private TableName name;
    private String filename;

    public Table(TableName name) {
        this.name = name;
        if (name.equals(TableName.INVERTED_INDEX_TITLE)) {
            this.filename = "inverted_index_title";
        } else if (name.equals(TableName.INVERTED_INDEX_BODY)) {
            this.filename = "inverted_index_body";
        } else if (name.equals(TableName.TERM_TO_ID)) {
            this.filename = "word_to_id";
        } else if (name.equals(TableName.PAGES)) {
            this.filename = "pages";
        } else if (name.equals(TableName.URL_to_ID)) {
            this.filename = "url_to_id";
        } else {
            this.filename = "undefined_File";
        }

    }

    public boolean checkType(Object obj) {
        if (name.equals(TableName.INVERTED_INDEX_TITLE)) {
            return obj instanceof Term;
        } else if (name.equals(TableName.INVERTED_INDEX_BODY)) {
            return obj instanceof Term;
        } else if (name.equals(TableName.TERM_TO_ID)) {
            return obj instanceof String;
        } else if (name.equals(TableName.PAGES)) {
            return obj instanceof Page;
        } else if (name.equals(TableName.URL_to_ID)) {
            return obj instanceof String;
        } else {
            return true;
        }
    }

    // Getters

    public TableName getName() {
        return name;
    }

    public String getFilename() {
        return filename;
    }

    // Setters

    public void setName(TableName name) {
        this.name = name;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }
}