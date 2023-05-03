package project;

import java.io.IOException;
import java.net.URL;

import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import jdbm.helper.FastIterator;
import jdbm.htree.HTree;

public class DatabaseManager {
    private RecordManager recman;
    private HTree db;
    private Table table;

    // Constructors
    public DatabaseManager(TableName tableName) throws IOException {
        this.table = new Table(tableName);
        String filename = "project/database/" + table.getFilename();
        recman = RecordManagerFactory.createRecordManager(filename);
    }

    // Link to existing database file
    public void linkDatabase() throws IOException {
        String filename = table.getFilename();
        long recid = recman.getNamedObject(filename);
        db = HTree.load(recman, recid);
    }

    public void createDatabase() throws IOException {
        String filename = table.getFilename();
        db = HTree.createInstance(recman);
        recman.setNamedObject(filename, db.getRecid());
    }

    // Getters

    public RecordManager getRecman() {
        return recman;
    }

    public HTree getDb() {
        return db;
    }

    public Table getTable() {
        return table;
    }

    // Setters

    public void setRecman(RecordManager recman) {
        this.recman = recman;
    }

    public void setDb(HTree db) {
        this.db = db;
    }

    public void setTable(Table table) {
        this.table = table;
    }

    public void update(String key, Object val) throws IOException {
        if (table.checkType(val)) {
            db.put(key, val);
            save();
            return;
        }
        throw new IllegalArgumentException("Type of value mismatch.");
    }

    public Object get(String key) throws IOException {
        return db.get(key);
    }

    public Object getPage(URL url) throws IOException {
        FastIterator iter = db.keys();
        String key;
        while ((key = (String) iter.next()) != null) {
            if (((Page) db.get(key)).getUrl() == url) {
                return db.get(key);
            }
        }
        return null;
    }

    public FastIterator getIterator() throws IOException{
        return db.keys();
    }

    public void remove(String key) throws IOException {
        db.remove(key);
        save();
    }

    private void save() throws IOException {
        recman.commit();
    }
}
