import java.lang.*;
import java.util.*;
import java.io.*;

public class bplustree {
    int order;   // tree order
    Indexnd root;  // tree's root
    Leafnd begleaf;  //  leftmost leaf of the tree
    
    //default constructor
    public bplustree(int order) {
        this.order = order;
        this.root = null;
    }

    // Dictionary instance involves dictionary pairs of keys and values
    public class Dictionaryinstance implements Comparable<Dictionaryinstance> {
        int key;
        double value;

        //default constructor
        public Dictionaryinstance(int key, double value) {
            this.key = key;
            this.value = value;
        }
        
        //compare operation for between two instances where comparison is done by the key and not the value
        @Override
        public int compareTo(Dictionaryinstance o) {
            if (key == o.key) { return 0; }
            else if (key > o.key) { return 1; }
            else { return -1; }
        }
    }

    public class Node {
        Indexnd parent;
    }
    // indexnd is a node where it only holds keys and not the values
    private class Indexnd extends Node {
        Integer[] keys;   //array of keys
        Node[] childp;   //array of child pointers
        int deg;            // current number of child pointers
        int mindeg;         // the minimum number of child pointers that a node can have
        int maxdeg;         // the maximum number of child pointers that a node can have
        Indexnd leftsib;    //left sibling used for borrow
        Indexnd rightsib;  //right sibling used for borrow
        
        //default constructor
        private Indexnd(int order, Integer[] keys) {
            this.maxdeg = order;
            this.mindeg = (int)Math.ceil(order/2.0);
            this.deg = 0;
            this.keys = keys;
            this.childp = new Node[this.maxdeg+1];

        }
            
        // constructor used to create new indexnodes with existing pointers in cases like splitting 
        private Indexnd(int order, Integer[] keys, Node[] pointers) {
            this.maxdeg = order;
            this.mindeg = (int)Math.ceil(order/2.0);
            this.deg = searchnullp(pointers);
            this.keys = keys;
            this.childp = pointers;

        }

        private boolean checkoverfull() {  // check if node is overfull
            return this.deg == maxdeg + 1;
        }
        private boolean checklend() {   // check if node can lend 
            return this.deg > this.mindeg; 
        }
        private boolean checkdef() {  // check if node is deficient
            return this.deg < this.mindeg;
        }

        private boolean checkmerge() {  // check if node can be used for merging
            return this.deg == this.mindeg; 
        }

        private void insertKey(int key) {  //insert a given key into index node
            for(int i=0;i<this.keys.length;i++){
                if(this.keys[i]==null){
                    this.keys[i]=key;
                    break;
                }
            }
            sortKeyswithnull(this.keys);
        }

        private int findindpointer(Node pointer) {  //given a pointer find the index of that pointer in the index node
            for (int i = 0; i < childp.length; i++) {
                if (childp[i] == pointer) { return i; }
            }
            return -1;
        }

        private void addchildpointer(Node pointer) {  //insert a child pointer
            this.childp[deg] = pointer;
            this.deg++;
        }

        private void insertposchildpointer(Node pointer, int index) { //insert a child pointer at a given position
            for (int i = deg - 1; i >= index ;i--) {
                childp[i + 1] = childp[i];
            }
            this.childp[index] = pointer;
            this.deg++;
        }

        private void removeKey(int index) { this.keys[index] = null; } //remove a key at a position 
        
        private void removePointer(int index) {  //remove a pointer at a position 
            this.childp[index] = null;
            this.deg--;
        }

        private void removePointer(Node pointer) { //remove the given pointer
            for (int i = 0; i < childp.length; i++) {
                if (childp[i] == pointer) { this.childp[i] = null; }
            }
            this.deg--;
        }
        //cases where a null pointers happens to appear in the middle of childpointers (happens in removing a pointer)
        // so all remaining pointers after are needed to be moved one spot to the left
        private void removenullindex(){
            boolean start=false;
            for(int i=0;i<childp.length-1;i++){
                if(childp[i]==null || start){
                    childp[i]=childp[i+1];
                    start=true;
                }
            }
        }
        
    }
    // leafnd stores the actual dictionary keys and values
    public class Leafnd extends Node {
        Dictionaryinstance[] dictionary;  //array which stores dictionary instances
        int np;  //current number of dictionary instances
        int minnp; //minimum number of dictionary instances
        int maxnp; //maximum number of dictionary instances
        Leafnd leftsib;  // left sibling used for range search
        Leafnd rightsib; //right sibling used for range search

        //default constructor
        public Leafnd(int order, Dictionaryinstance dp) {
            this.maxnp = order - 1;
            this.minnp = (int)(Math.ceil(order/2.0) - 1);
            this.np = 0;
            this.dictionary = new Dictionaryinstance[order];
            this.insert(dp);

        }
        //constructor used for creating a new leafnode with given dictionary instance array used in cases like split
        public Leafnd(int order, Dictionaryinstance[] dps, Indexnd parent) {
            this.maxnp = order - 1;
            this.minnp = (int)(Math.ceil(order/2.0) - 1);
            this.dictionary = dps;
            this.np = searchnulld(dps);
            this.parent = parent;
        }
        public boolean checkfull() { // check if node is full
            return np == maxnp;
        }

        public boolean checklend() { // check if node can lend
            return np > minnp;
        }

        public boolean checkdef() { // check if node is deficient
            return np < minnp;
        }

        public boolean checkmerge() {  // check if node can be used for merging
            return np == minnp;
        }
        // insert a given dictionary instance into node
        public boolean insert(Dictionaryinstance dp) {
            if (this.checkfull()) { // check the node is full
                return false;
            } else {

                this.dictionary[np] = dp; // Insert given dictionary instance
                np++;   // the number of dictionary instances the node holds goes up by 1
                sortdict(this.dictionary); //sort the dictionary after insertion
                return true;
            }
        }
        
        public void delete(int index) { //delete a dictionary instance at a given postiton
            this.dictionary[index] = null; 
            np--;
        }

    }
    
    //binary search for searching for a key in a given dictionary instance given the number of instances in the node
    private int binarySearch(Dictionaryinstance[] dps, int np, int key1) {
        Comparator<Dictionaryinstance> c = new Comparator<Dictionaryinstance>() {
            @Override
            public int compare(Dictionaryinstance o1, Dictionaryinstance o2) {
                Integer a = Integer.valueOf(o1.key);
                Integer b = Integer.valueOf(o2.key);
                return a.compareTo(b);
            }
        };
        return Arrays.binarySearch(dps, 0, np, new Dictionaryinstance(key1, 0), c);
    }
    // sort with null values in a given dictionary instance
    private void sortdict(Dictionaryinstance[] dictionary) {
        Arrays.sort(dictionary,new Comparator<Dictionaryinstance>() {
            @Override
            public int compare(Dictionaryinstance o1, Dictionaryinstance o2) {
                if (o1 == null && o2 == null) { return 0; }
                if (o1 == null) { return 1; }
                if (o2 == null) { return -1; }
                return o1.compareTo(o2);
            }
        });
    }
    // sort with null values in a given keys array
    private void sortKeyswithnull(Integer[] dictionary) {
        Arrays.sort(dictionary, new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                if (o1 == null && o2 == null) { return 0; }
                if (o1 == null) { return 1; }
                if (o2 == null) { return -1; }
                return o1.compareTo(o2);
            }
        });
    }

    private Leafnd findLeafnd(int key) {  // returns the leaf node given a key
        
        Integer[] keys = this.root.keys; //root keys
        int i;
        // compare root keys with given key and if key is smaller than any key
        // then it breaks to give position of the child pointer that key might be contained
        for (i = 0; i < this.root.deg - 1; i++) {
            if (key < keys[i]) {
                break;
            }
        }
        Node child = this.root.childp[i];  // child pointer where key might be present
        if (child instanceof Leafnd) {
            return (Leafnd)child;     // if child pointer is leaf then this terminates the search and returns leaf
        } else {
            return findLeafnd((Indexnd)child, key); // if child pointer is index then the next immediate level needs to be searched
        }
    }

    private Leafnd findLeafnd(Indexnd node, int key) { //function to go though index nodes and parent exists
        
        Integer[] keys = node.keys; //index node keys
        int i;
        
        for (i = 0; i < node.deg - 1; i++) { // similar to above
            if (key < keys[i]) { break; }
        }
        Node childNode = node.childp[i];
        if (childNode instanceof Leafnd) {
            return (Leafnd)childNode;
        } else {
            return findLeafnd((Indexnd)node.childp[i], key); 
        }
    }
    // search for the first null in given keys used for creating degree in a indexnode, used in delete borrow
    private int searchnullk(Integer[] keys) {
        for (int i = 0; i <  keys.length; i++) {
            if (keys[i] == null) { return i; }
        }
        return -1;
    }

    // search for the first null in given pointers used for creating degree in a indexnode, used in split
    private int searchnullp(Node[] pointers) {
        for (int i = 0; i <  pointers.length; i++) {
            if (pointers[i] == null) { return i; }
        }
        return -1;
    }
    // search for the first null in given dictionary instances used for creating number of instances in a leafnode, used in split
    private int searchnulld(Dictionaryinstance[] dps) {
        for (int i = 0; i <  dps.length; i++) {
            if (dps[i] == null) { return i; }
        }
        return -1;
    }
    private int findindpointer(Node[] pointers, Leafnd node) { // given pointers (child pointers) find for a given leaf node
        int i;
        for (i = 0; i < pointers.length; i++) {
            if (pointers[i] == node) { break; }
        }
        return i;
    }

    // shift pointers by a given amount to left or right ,used in delete borrow
    private void shiftpointersamt(Node[] pointers, int amount) {
        Node[] newpointerarray = new Node[this.order + 1];
        for (int i = amount; i < pointers.length; i++) {
            newpointerarray[i - amount] = pointers[i];
        }
        pointers = newpointerarray;
    }
    // given position of split, split the key array into two halves with one half staying in original array-
    // deleting the keys from original array and creating a new array for the other half excluding the middle element
    // and returning them
    private Integer[] splitkeys(Integer[] keys, int split) {

        Integer[] halfKeys = new Integer[this.order];
        keys[split] = null; //key becomes null as it goes into upper level that splits the node

        for (int i = split + 1; i < keys.length; i++) {
            halfKeys[i - split - 1] = keys[i]; //new key array with second half of keys
            keys[i] = null; // removing the second half of keys
        }

        return halfKeys;
    }

    //given position of split, split the child pointers array into two halves with one half staying in original array-
    // deleting the pointers from original array and creating a new array for the other half excluding the middle element
    // and returning them 
    private Node[] splitchildp(Indexnd in, int split) {

        Node[] pointers = in.childp;
        Node[] newhalfpointers = new Node[this.order + 1];
        
        for (int i = split + 1; i < pointers.length; i++) {  
            newhalfpointers[i - split - 1] = pointers[i];  //new pointer array with second half of pointers 
            in.removePointer(i);            // removing the second half of pointers
        }

        return newhalfpointers;
    }

    // given position of split, split the dictionary array into two halves with one half staying in original array-
    // deleting the instances from original array and creating a new array for the other half including the middle element 
    // and returning them 
    private Dictionaryinstance[] splitdict(Leafnd ln, int split) {

        Dictionaryinstance[] dictionary = ln.dictionary;
        Dictionaryinstance[] newhalfdict = new Dictionaryinstance[this.order];
        
        for (int i = split; i < dictionary.length; i++) {
            newhalfdict[i - split] = dictionary[i]; //new dictionary array with second half of dictionary instances
            ln.delete(i);  // removing the second half of dictionary
        }

        return newhalfdict;
    }

    //split the index node into two nodes by taking care of pointers
    private void splitIndexnd(Indexnd in) {

        Indexnd parent = in.parent;

        int midpoint = (int)Math.ceil((this.order + 1) / 2.0) - 1; // take midpoint
        int newParentKey = in.keys[midpoint];
        // split so that the the first half stays in its old array and the second half in the new array
        Integer[] halfKeys = splitkeys(in.keys, midpoint);
        Node[] newhalfpointers = splitchildp(in, midpoint);


        in.deg = searchnullp(in.childp); // Change deg of original Indexnd in
        Indexnd sibling = new Indexnd(this.order, halfKeys, newhalfpointers); // Create new sibling index node with second half of keys and pointers
        for (Node pointer : newhalfpointers) {
            if (pointer != null) { pointer.parent = sibling; } //update the second half pointers parent to be the new node
        }

        // Make index nodes siblings of one another
        sibling.rightsib = in.rightsib;
        if (sibling.rightsib != null) {
            sibling.rightsib.leftsib = sibling;
        }
        in.rightsib = sibling;
        sibling.leftsib = in;

        if (parent == null) {
            // Create new root node and add midpoint key and pointers to nodes that got split
            Integer[] keys = new Integer[this.order];
            keys[0] = newParentKey;
            Indexnd newRoot = new Indexnd(this.order, keys);
            newRoot.addchildpointer(in);
            newRoot.addchildpointer(sibling);
            this.root = newRoot;
            in.parent = newRoot;  //change parent pointers of respective child nodes
            sibling.parent = newRoot;

        } else {
            //add midpoint key to parent
            parent.keys[parent.deg - 1] = newParentKey;
            Arrays.sort(parent.keys, 0, parent.deg);
            // add new indexnode pointer to parent
            int pointerind = parent.findindpointer(in) + 1;
            parent.insertposchildpointer(sibling, pointerind);
            sibling.parent = parent;
        }
    }

    
    private void managedef(Indexnd in){
        Indexnd sibling;
        Indexnd parent=in.parent; //parent to given indexnode

        if (this.root == in) { // given node is root
            for (int i = 0; i < in.childp.length; i++) {
                if (in.childp[i] != null) {
                    if (in.childp[i] instanceof Indexnd) { // case where after merging the child pointers
                                                            // would having the current root and root would
                                                                // be updated
                        this.root = (Indexnd)in.childp[i];
                        this.root.parent = null;
                    } else if (in.childp[i] instanceof Leafnd) { // case where the root is merged into leaf node (low height b+tree)
                        this.root = null;
                    }
                }
            }
        }
        //Borrow from the left sibling
        else if (in.leftsib != null && in.leftsib.parent==in.parent && in.leftsib.checklend()) {
            sibling = in.leftsib;
            int index = parent.findindpointer(in); // index of current node
            in.insertKey(parent.keys[index - 1]); //insert parent key in to current node
            int parentkey = searchnullk(sibling.keys); // index of first null in sibling keys
            //sibling.childp[parentkey].parent=sibling;
            in.insertposchildpointer(sibling.childp[parentkey],0); // insert the right most child pointer into current node
            sibling.removePointer(sibling.childp[parentkey]); // remove the right most child pointer from sibling
            parent.keys[index - 1] = sibling.keys[parentkey - 1]; //change key of parent from sibling right most key
            sibling.keys[parentkey - 1] = null; // after change remove that key

        }
        //Borrow from the right sibling
        else if (in.rightsib != null && in.rightsib.parent==in.parent && in.rightsib.checklend()) {
            sibling = in.rightsib;
            int index = parent.findindpointer(in); // index of current node
            in.insertKey(parent.keys[index]); //insert parent key in to current node
            //sibling.childp[0].parent=sibling;
            in.insertposchildpointer(sibling.childp[0],in.deg); //insert the left most pointer into the right most position of current node
            sibling.removePointer(0); // after insert remove that pointer
            shiftpointersamt(sibling.childp,1); //after removing that pointer shift the pointers to the right
            parent.keys[index] = sibling.keys[0];// insert sibling left most key into parent
            sibling.removeKey(0); // remove key from sibling
            sortKeyswithnull(sibling.keys); //adjust the keys to move null

        }
        else if (in.leftsib != null && in.leftsib.parent==in.parent && in.leftsib.checkmerge()) {
            sibling = in.leftsib;
            int pointerind = parent.findindpointer(in); // index of current pointer
            sibling.insertKey(parent.keys[pointerind-1]); // insert parent key
            parent.removeKey(pointerind - 1); // remove that key from parent
            sortKeyswithnull(parent.keys);// sort the keys if any in root
            parent.removePointer(in);// remove the pointer to current node
            parent.removenullindex();// case where the merging is happening in middle then this leaves childpointer array to have null
                                      // so this removes that null from child pointer array
            for(int i=0;i<in.keys.length;i++){
                if(in.keys[i]!=null){
                    sibling.insertKey(in.keys[i]); // insert keys from current node to sibling
                }}
            for(int i=0;i<in.childp.length;i++){
                if(in.childp[i]!=null){
                    in.childp[i].parent=sibling; // making sibling the parent of all current node child pointers
                    sibling.addchildpointer(in.childp[i]); // and inserting them into sibling

                }}
            sibling.rightsib = in.rightsib; //update siblings

        }
        else if (in.rightsib != null && in.rightsib.parent==in.parent && in.rightsib.checkmerge()) {
            sibling = in.rightsib;
            int pointerind = parent.findindpointer(in); // index of current pointer
            sibling.insertKey(parent.keys[pointerind]); // insert parent key
            parent.removeKey(pointerind); // remove that key from parent
            sortKeyswithnull(parent.keys); // sort the keys if any in root
            parent.removePointer(in);// remove the pointer to current node
            parent.removenullindex();// case where the merging is happening in middle then this leaves childpointer array to have null
                                    // so this removes that null from child pointer array
            for(int i=0;i<in.keys.length;i++){
                if(in.keys[i]!=null){
                    sibling.insertKey(in.keys[i]); //insert keys from current node to sibling
                }}
            for(int i=in.childp.length-1;i>=0;i--){
                if(in.childp[i]!=null){
                    in.childp[i].parent=sibling; // making sibling the parent of all current node child pointers
                    sibling.insertposchildpointer(in.childp[i],0); // and inserting them into sibling at left most position
                }}
            sibling.leftsib = in.leftsib; // update siblings

        }
        if (parent != null && parent.checkdef()) { // upto root and check each level index node deficiency
            managedef(parent);
        }

    }
    public void insert(int key, double value){
        if (this.begleaf==null) { //check the tree is empty
            //if root is null create the first node which is leaf
            Leafnd ln = new Leafnd(this.order, new Dictionaryinstance(key, value));
            this.begleaf = ln;

        } else {
            Leafnd ln = (this.root == null) ? this.begleaf :     //leaf node search for insert
                    findLeafnd(key);

            if (!ln.insert(new Dictionaryinstance(key, value))) { //after insert check if it overfull
                // if overfull then add the dictionary instance sort and split
                ln.dictionary[ln.np] = new Dictionaryinstance(key, value);
                ln.np++;
                sortdict(ln.dictionary);
                int midpoint = (int)Math.ceil((this.order + 1) / 2.0) - 1;
                Dictionaryinstance[] newhalfdict = splitdict(ln, midpoint);

                if (ln.parent == null) {
                    // if there is no parent the new internal node must be created after split
                    Integer[] parent_keys = new Integer[this.order];
                    parent_keys[0] = newhalfdict[0].key;
                    Indexnd parent = new Indexnd(this.order, parent_keys);
                    ln.parent = parent;
                    parent.addchildpointer(ln);

                } else {
                    // add key to parent if already exists
                    int newParentKey = newhalfdict[0].key;
                    ln.parent.keys[ln.parent.deg - 1] = newParentKey;
                    Arrays.sort(ln.parent.keys, 0, ln.parent.deg);
                }
                Leafnd newLeafnd = new Leafnd(this.order, newhalfdict, ln.parent);  // Create new Leafnd with second half dictionary
                int pointerind = ln.parent.findindpointer(ln) + 1;
                ln.parent.insertposchildpointer(newLeafnd, pointerind); //make pointer from parent to new leaf

                // Make leaf nodes siblings to each other
                newLeafnd.rightsib = ln.rightsib;
                if (newLeafnd.rightsib != null) {
                    newLeafnd.rightsib.leftsib = newLeafnd;
                }
                ln.rightsib = newLeafnd;
                newLeafnd.leftsib = ln;

                if (this.root == null) {
                    // set root to be the parent of the leaf node
                    this.root = ln.parent;

                } else {
                    // inserting into index node can make the indexnode overfull
                    Indexnd in = ln.parent;
                    while (in != null) { // loop until the root
                        if (in.checkoverfull()) { // check each level of index node
                            splitIndexnd(in); // split the overfull indexnode
                        } else {
                            break;
                        }
                        in = in.parent;
                    }
                }
            }
        }
    }

    public void delete(int key) {
        if (this.begleaf==null) {//check the tree is empty
            System.err.println("The B+ tree is empty, it has no nodes.");
        } else {
            // search the leaf node where the delete needs to be performed
            Leafnd ln = (this.root == null) ? this.begleaf : findLeafnd(key);
            int dpIndex = binarySearch(ln.dictionary, ln.np, key); //search for exact key in leafnode

            if (dpIndex < 0) {
                System.err.println("There is no such Key.");

            } else {
                ln.delete(dpIndex);// delete the given dictionary instance
                if (ln.checkdef()) { //check of node is deficient

                    Leafnd sibling;
                    Indexnd parent = ln.parent;
                    // Check the left sibling, then the right sibling for borrow
                    if (ln.leftsib != null && ln.leftsib.parent == ln.parent && ln.leftsib.checklend()) {
                        sibling = ln.leftsib;
                        Dictionaryinstance borrowedDi = sibling.dictionary[sibling.np - 1];
                        ln.insert(borrowedDi); //insert dictionary instance
                        sibling.delete(sibling.np - 1); //delete dictionary instance from the sibling
                        int pointerind = findindpointer(parent.childp, ln);
                        if (!(borrowedDi.key >= parent.keys[pointerind - 1])) { // If required change key in parent
                            parent.keys[pointerind - 1] = ln.dictionary[0].key;
                        }

                    } else if (ln.rightsib != null && ln.rightsib.parent == ln.parent && ln.rightsib.checklend()) {
                        // sort required as in case where delete happens in one index and that index becomes the number of
                        // dictionary instances after delete then without sorting this can lead to deletion of another instance
                        sortdict(ln.dictionary);
                        sibling = ln.rightsib;
                        Dictionaryinstance borrowedDi = sibling.dictionary[0];
                        ln.insert(borrowedDi);//insert dictionary instance
                        sibling.delete(0);  //delete dictionary instance from the sibling
                        sortdict(sibling.dictionary);
                        int pointerind = findindpointer(parent.childp, ln);
                        if (!(borrowedDi.key < parent.keys[pointerind])) {  // If required change key in parent
                            parent.keys[pointerind] = sibling.dictionary[0].key;
                        }

                    }
                    // check the left sibling and then right sibling for merge
                    else if (ln.leftsib != null && ln.leftsib.parent == ln.parent && ln.leftsib.checkmerge()) {
                        sibling = ln.leftsib;
                        int pointerind = findindpointer(parent.childp, ln);
                        parent.removeKey(pointerind - 1); //delete key from the parent
                        parent.removePointer(ln); // delete pointer from the parent
                        for(int i=0;i<ln.dictionary.length;i++){
                            if(ln.dictionary[i]!=null){
                                sibling.insert(ln.dictionary[i]);  // inserting the instances from to be merged node to sibling
                            }}
                        sibling.rightsib = ln.rightsib;  // update pointers
                        if (parent.checkdef()) {
                            managedef(parent);  // handle the deficiency in next level
                        }

                    } else if (ln.rightsib != null && ln.rightsib.parent == ln.parent && ln.rightsib.checkmerge()) {
                        sibling = ln.rightsib;
                        int pointerind = findindpointer(parent.childp, ln);
                        parent.removeKey(pointerind); //delete key from the parent
                        parent.removePointer(pointerind); // delete pointer from the parent
                        for(int i=0;i<ln.dictionary.length;i++){
                            if(ln.dictionary[i]!=null){
                                sibling.insert(ln.dictionary[i]);  // inserting the instances from to be merged node to sibling
                            }}
                        sibling.leftsib = ln.leftsib; // update pointer
                        if (sibling.leftsib == null) {
                            begleaf = sibling; // if first leaf from left then update the leaf node
                        }
                        if (parent.checkdef()) {
                            managedef(parent); // handle the deficiency in next level
                        }
                    }

                } else if (this.root == null && this.begleaf.np == 0) {
                    // after delete the leaf node is empty (only leaf node in tree)
                    this.begleaf = null;

                } else {
                    // after delete the leaf node may needs to be sorted (only leaf node in tree)
                    sortdict(ln.dictionary);

                }
            }
        }
    }

    public Double search(int key) {

        if (this.begleaf==null) //check the tree is empty
             { return null; } // b+tree is empty
        Leafnd ln = (this.root == null) ? this.begleaf : findLeafnd(key); // search leafnode that contains key
        Dictionaryinstance[] dps = ln.dictionary;
        int index = binarySearch(dps, ln.np, key);// search key in that particular dictionary
        if (index < 0) {
            return null;
        } else {
            return dps[index].value;
        }
    }

    public ArrayList<Double> search(int lowerBound, int upperBound) {
        
        ArrayList<Double> values = new ArrayList<Double>(); // Instantiate Double arraylist for values
        Leafnd currNode = this.begleaf; //the left most leaf used for range search by iterating
        while (currNode != null) { //check each node
            Dictionaryinstance dps[] = currNode.dictionary;
            for (Dictionaryinstance dp : dps) { // check leaf nodeeach dictionary
                if (dp == null) { break; } // stop when null occurs in that leaf
                if (lowerBound <= dp.key && dp.key <= upperBound) { // if leaf contains values given in range 
                    values.add(dp.value); // then add them to arraylist
                }
            }
            currNode = currNode.rightsib; // traverse the right sibling

        }

        return values;
    }

    public static void main(String[] args) {
        
        if (args.length != 1) {
            System.err.println("usage: java bplustree <file_name>");
            System.exit(-1);
        }
        String fileName = args[0]; //read from file
        try {
            File file = new File(System.getProperty("user.dir") + "/" + fileName); // input file read
            Scanner sc = new Scanner(file);
            FileWriter logger = new FileWriter("output_file.txt", false); //output file write
            boolean firstLine = true;
            bplustree bpt = null; // intital b+tree
            while (sc.hasNextLine()) { //check each line
                String line = sc.nextLine().replace(" ", "");
                String[] tokens = line.split("[(,)]");
                switch (tokens[0]) {
                    case "Initialize": // B+ tree of order m is initialized
                        bpt = new bplustree(Integer.parseInt(tokens[1]));
                        break;
                    case "Insert":  // insert a key and its value into b+ tree
                        bpt.insert(Integer.parseInt(tokens[1]), Double.parseDouble(tokens[2]));
                        break;
                    case "Delete": //delete a key and its value from b+ tree
                        bpt.delete(Integer.parseInt(tokens[1]));
                        break;
                    case "Search":
                        String result = "";
                        if (tokens.length == 3) { //do a range search
                            ArrayList<Double> values = bpt.search(Integer.parseInt(tokens[1]), Integer.parseInt(tokens[2]));
                            if (values.size() != 0) {
                                for (double v : values) { result += v + ", "; }
                                result = result.substring(0, result.length() - 2); //write the result as string
                            } else {
                                result = "Null";
                            }

                        }
                        else {
                            Double value = bpt.search(Integer.parseInt(tokens[1])); //search for the given key
                            result = (value == null) ? "Null" : Double.toString(value);
                        }

                        if (firstLine) {
                            logger.write(result); //write result into output file
                            firstLine = false;
                        } else {
                            logger.write("\n" + result);
                        }
                        logger.flush();

                        break;
                    default:
                        throw new IllegalArgumentException("\"" + tokens[0] +
                                "\"" + " is an unacceptable input.");
                }
            }
            logger.close(); //close output file

        } catch (FileNotFoundException e) {
            System.err.println(e);
        } catch (IllegalArgumentException e) {
            System.err.println(e);
        } catch (IOException e) {
            System.err.println(e);
        }
    }
}