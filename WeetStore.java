/**
 * Again, to store the weets, I am using a hash table. The reasoning behind this
 * is the same as it is for the user store. In recap, speed is the main priority
 * with storage not being an immediate issue as user experience is the most
 * important thing to increase.
 *
 */

package uk.ac.warwick.java.cs126.services;

import uk.ac.warwick.java.cs126.models.User;
import uk.ac.warwick.java.cs126.models.Weet;

import java.io.BufferedReader;
import java.util.Date;
import java.io.FileReader;
import java.text.ParseException;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;


public class WeetStore implements IWeetStore {

    /**
     * Bucket: Class used in the hash table as the bucket. Implimented as a linked
     * list, with a bucket linking to the next bucket along in the chain.
     * Buckets are added to the end of the chain.
     *
     */
    private class Bucket<Weet> {
        // bucket stores weet and next bucket in in list
        public Weet weet;
        // will be null if last bucket in list
        public Bucket<Weet> next;

        public Bucket(Weet weet, Bucket<Weet> next) {
            this.weet = weet;
            this.next = next;
        }
    }

    private Bucket<Weet>[] table;
    private int tableCapacity;
    private int tableSize;

    public WeetStore() {
        tableCapacity = 130147;
        table = new Bucket[tableCapacity];
        tableSize = 0;
    }

    /**
     * addWeet: Add a weet to the hash table. Method returns true if the weet is
     * succesfully added to the table (does not already exist) and false if not
     * (weets already exists with that id in table).
     *
     * Algorithm Complexity: O(k) + O(k) = O(k)
     * where k is the number of buckets in list (On average is tableSize/tableCapacity)
     *
     */

    public boolean addWeet(Weet weet) {
        // check if a weet already exists with the given id
        if (getWeet(weet.getId()) != null) {
            return false;
        }
        // create a bucket for the weet
        Bucket<Weet> weetBucket = new Bucket<>(weet, null);
        // calculate the hash and get the head bucket
        int hash = weet.getId() % tableCapacity;
        Bucket<Weet> bucket = table[hash];
        // add the bucket to the list
        if (bucket == null) {
            table[hash] = weetBucket;
        } else {
            while (bucket.next != null) {
                bucket = bucket.next;
            }
            bucket.next = weetBucket;
        }
        // incrase table size by one
        tableSize++;
        return true;
    }

    /**
     * getWeet: return a weet from the hash table with the given id. Return null
     * if a weet with the id does not exist.
     *
     * Algorithm Complexity: O(k)
     * where k is the number of buckets in list (On average is tableSize/tableCapacity)
     *
     */

    public Weet getWeet(int wid) {
        // calculate the hash and retrive the head bucket from the table
        int hash = wid % tableCapacity;
        Bucket<Weet> bucket = table[hash];
        // iterate through list of buckets
        while (bucket != null) {
            if (bucket.weet.getId() == wid) {
                return bucket.weet;
            }
            bucket = bucket.next;
        }
        // no weet found with given wid, return null
        return null;
    }

    /**
     * getWeets: return an array of every weet, sorted by date such hat the most
     * recent weet is the first item in the list.
     *
     * Algorithm Complexity: O(nk) + O(n logn) = O(n logn)
     * where n is the number of weets in the table
     *  where k is the number of buckets in list (On average is tableSize/tableCapacity)
     *
     */

    public Weet[] getWeets() {
        // create an array of size of num of weets in table
        Weet[] weets = new Weet[tableSize];
        int cur_index = 0;
        // iterate through the hash table, and then the list of buckets
        for (int i=0; i<tableCapacity; i++) {
            // get the head bucket at each table index
            Bucket<Weet> bucket = table[i];
            while (bucket != null) {
                weets[cur_index++] = bucket.weet;
                bucket = bucket.next;
            }
        }
        // sort the array by date weeted
        sort(weets, tableSize);
        return weets;
    }

    /**
     * getWeetsByUser: return an array of all weets made by the given user,
     * sorted such that the most recent weet is first.
     *
     * Algorithm Complexity: O(n logn) + O(n) + O(m) = O(n logn)
     * where n is the number of weets in the table
     * where m is the number of weets by the given user
     *
     */

    public Weet[] getWeetsByUser(User usr) {
        // get all weets in order of date added
        Weet[] allWeets = getWeets();
        // create a temp array to hold tweets by given user
        Weet[] tempWeets = new Weet[tableSize];
        int weet_count = 0;
        // get the id of the user
        int userId = usr.getId();
        // iterate through allWeets, checking if any are from the given user
        for (int i=0; i<tableSize; i++) {
            if (allWeets[i].getUserId() == userId) {
                tempWeets[weet_count++] = allWeets[i];
            }
        }
        // remove the null elements from the array and return
        Weet[] weets = new Weet[weet_count];
        for (int j=0; j<weet_count; j++) {
            weets[j] = tempWeets[j];
        }
        return weets;
    }

    /**
     * getWeetsContaining: return an array of all weets containing the given
     * query, or null if no weets contain the query. The weets are sorted by
     * date such that the most recent weet is first. Search queries are case
     * insensitive.
     *
     * Algorithm Complexity: O(n logn) + O(n) + O(m) = O(n logn)
     * where n is the number of weets in the table
     * where m is the number of weets containing the given query
     *
     */

    public Weet[] getWeetsContaining(String query) {
        // get all weets in order of date added
        Weet[] allWeets = getWeets();
        // create a temp array to hold tweets containing query
        Weet[] tempWeets = new Weet[tableSize];
        int weet_count = 0;
        // iterate through allWeets, checking if they contain the query
        for (int i=0; i<tableSize; i++) {
            if (allWeets[i].getMessage().contains(query)) {
                tempWeets[weet_count++] = allWeets[i];
            }
        }
        // check if there were any weets found containing query
        if (weet_count == 0) {
            return null;
        } else {
            // otherwise remove the null elements from the temp array
            Weet[] weets = new Weet[weet_count];
            for (int j=0; j<weet_count; j++) {
                weets[j] = tempWeets[j];
            }
            return weets;
        }
    }

    /**
     * getWeetsOn: return an array of all the weets made on the given date. The
     * weers must be sorted such that the most recent weet is first in the list.
     *
     * Algorithm Complexity: O(n logn) + O(n) + O(m) = O(n logn)
     * where n is the number of weets in the table
     * where m is the number of weets create on the given date
     *
     */

    public Weet[] getWeetsOn(Date dateOn) {
        // get all weets in order of date added
        Weet[] allWeets = getWeets();
        // create a temp array to hold tweets on date
        Weet[] tempWeets = new Weet[tableSize];
        int weet_count = 0;
        // format dateOn so that it can be compared TODO: use exceptions
        String dateOnFormatted = dateOn.toString().substring(0, 10);
        // iterate through allWeets, checking if any were weeted on the same day
        for (int i=0; i<tableSize; i++) {
            // format the date weetd of the weet
            String weetDateFormatted = allWeets[i].getDateWeeted().toString().substring(0, 10);
            if (weetDateFormatted.equals(dateOnFormatted)) {
                tempWeets[weet_count++] = allWeets[i];
            }
        }
        // remove the null elements from the array and return
        Weet[] weets = new Weet[weet_count];
        for (int j=0; j<weet_count; j++) {
            weets[j] = tempWeets[j];
        }
        return weets;
    }

    /**
     * getWeetsBefore: return an array of all the weets that were made before
     * the given date. The weets are sorted such that the most recent is first
     * in the list.
     *
     * Algorithm Complexity: O(n logn) + O(n) + O(m) = O(n logn)
     * where n is the number of weets in the table
     * where m is the number of weets created before the given date
     *
     */

    public Weet[] getWeetsBefore(Date dateBefore) {
        // get all weets in order of date added
        Weet[] allWeets = getWeets();
        // create a temp array to hold tweets on date
        Weet[] tempWeets = new Weet[tableSize];
        int weet_count = 0;
        // iterate through allWeets, checking if the weet date if beofe the given date
        for (int i=0; i<tableSize; i++) {
            Date weetDate = allWeets[i].getDateWeeted();
            if (weetDate.before(dateBefore)) {
                tempWeets[weet_count++] = allWeets[i];
            }
        }
        // remove the null elements from the array and return
        Weet[] weets = new Weet[weet_count];
        for (int j=0; j<weet_count; j++) {
            weets[j] = tempWeets[j];
        }
        return weets;
    }

    /**
     * getTrending: return an array of the top 10 trending topics from from
     * weets. As per the FAQ, since there could be an arbitrary number of tags
     * in a weet so this method will extract them all.
     *
     * Algorithm Complexity: O(n logn) + O(nm)
     * where n is
     *
     */

    public String[] getTrending() { //TODO: FINISH
        // get all weets in order of date added
        Weet[] allWeets = getWeets();
        // create arrays to hold data about the topics
        String[] allTopics = new String[tableSize];
        int[] allTopicsCount = new int[tableSize];
        int topic_count = 0;
        // loop backwards through the weets so recently weeted at the end
        for (int i=tableSize-1; i>-1; i--) {
            // get the all of the tags in the weet
            String message = allWeets[i].getMessage();
            while (getTag(message) != null) {
                String tag = getTag(message);
                for (int j=0; j<topic_count; j++) {
                    // tag in allTopics, add one to count
                    if (allTopics[j].equals(tag)) {
                        allTopicsCount[j] += 1;
                    }
                    // tag not already in allTopics
                    if (j == topic_count-1) {
                        allTopics[j++] = tag;
                    }
                }
                // remove tag from the message and repeat process
                message.replace(tag, "");
            }
        }
        // // get the top 10 topics and return them
        // String topics = new String[10];
        // for (int j=0; j<10; j++) {
        //     topics[j] = allTopics[]
        // }
        return null;
    }

    /**
     * getTag: get the first occurance of a tag from the weet and return it.
     * Will only get the first tag even if there are multiple in the weet.
     *
     * Algorithm Complexity:
     * where n is
     *
     */
    private String getTag(String message) {
        return null;
    }

    /**
     * Methods for the sorting algorithm used to sort the weet by date created
     *
     * Algorithm Complexity: O(n logn)
     * where n is the number of elements in the array being sorted
     *
     */

    private static void sort(Weet[] users, int arraySize) {
        // recursion base - arraySize == 1
        if (arraySize < 2) {
            return;
        }
        int middle = arraySize / 2;
        // create temp arrays
        Weet[] left = new Weet[middle];
        Weet[] right = new Weet[arraySize - middle];
        // copy the users array to temp arrays
        for (int i=0; i<middle; i++) {
            left[i] = users[i];
        }
        for (int j=middle; j<arraySize; j++) {
            right[j - middle] = users[j];
        }
        // recursive call
        sort(left, middle);
        sort(right, arraySize - middle);
        // merge the sub arrays
        merge(users, left, right, middle, arraySize - middle);
    }

    private static void merge(Weet[] users, Weet[] l, Weet[] r, int leftSize, int rightSize) {
        int i = 0, j = 0, k = 0;
        // compare users date joined
        while (i < leftSize && j < rightSize) {
            if (l[i].getDateWeeted().compareTo(r[j].getDateWeeted()) >= 0) {
                users[k++] = l[i++];
            } else {
                users[k++] = r[j++];
            }
        }
        while (i < leftSize) {
            users[k++] = l[i++];
        }
        while (j < rightSize) {
            users[k++] = r[j++];
        }
    }

}
