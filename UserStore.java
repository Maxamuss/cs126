/**
 * I have decided to use a hash table for the userStore. I have choosen to do this
 * because of the data structure's speed. In a real-world situation, such as an
 * application like 'witter', user experience is the most important thing.Therefore
 * the speed, and thus user experience, is be the most important thing and the
 * main focus the user store implimentation.Because of this, I have priorites speed
 * over storage. Mass storage is becoming cheaper and the retriving of the data is
 * the expensive part so it makes sense to priorites this. As performance is the
 * main aim of this data structure, a large array is initially created and never
 * resized. I deal with collisions in the hash table by creating a linked list.
 * A disadvantage of using a hash table is the cost can be high when the amount of
 * data being stored is small. However, this may not be too bad of thing as it
 * leaves room to grow in the future and expand.
 *
 * The hash table capacity was choosen to be a large prime number. This is to
 * avoid the clustering of values into a small number of buckets. A more evenly
 * evenly distributed table will perform better as less iterations through the
 * list of buckets will have to be done when the hash has been calulated. The
 * hash is calculed by taking the remainder of the user's id after division by
 * the large prime number (table capacity).
 *
 * For the sorting of the users by date they were added, I use merge sort
 * algorithm. I use this as its Complexity is O(nlogn), which means it is
 * capable of sorting quickly through large amounts of data. however, merge sort
 * uses more space than other algorithms, but as I explained about, that is not
 * to much of an issue.
 *
 * 
 */

package uk.ac.warwick.java.cs126.services;

import uk.ac.warwick.java.cs126.models.User;

import java.util.Date;

public class UserStore implements IUserStore {


    /**
     * Bucket: Class used in the hash table as the bucket. Implimented as a linked
     * list, with a bucket linking to the next bucket along in the chain.
     * Buckets are added to the end of the chain.
     *
     */
    private class Bucket<User> {
        // bucket stores the user and the next bucket in the list
        public User user;
        // will be null if last bucket in list
        public Bucket<User> next;

        public Bucket(User user, Bucket<User> next) {
            this.user = user;
            this.next = next;
        }
    }

    private Bucket<User>[] table;
    private int tableCapacity;
    private int tableSize;

    public UserStore() {
        tableCapacity = 130147;
        table = new Bucket[tableCapacity];
        tableSize = 0;
    }

    /**
     * addUser: this method will add a new user to the hash table, returning
     * true if succesfully added (not already in the table) and false if not
     * (already in the table).
     *
     * Algorithm Complexity: O(k) + O(k) = O(k)
     * where k is the number of bucket in list (On average is tableSize/tableCapacity)
     *
     */

    public boolean addUser(User usr) {
        // check if the user exists already in the table
        if (getUser(usr.getId()) != null) {
            return false;
        }
        // create a bucket for the new user
        Bucket<User> userBucket = new Bucket<>(usr, null);
        // calculate the hash and get the head bucket
        int hash = usr.getId() % tableCapacity;
        Bucket<User> bucket = table[hash];
        // add the bucket to the list
        if (bucket == null) {
            table[hash] = userBucket;
        } else {
            while (bucket.next != null) {
                bucket = bucket.next;
            }
            bucket.next = userBucket;
        }
        // incease the table size by one
        tableSize++;
        return true;
    }

    /**
     * getUser: method used to return a user from the hash table given the id
     * of the user.
     *
     * Algorithm Complexity: O(k)
     * where k is the number of buckets in list (On average is tableSize/tableCapacity)
     *
     */

    public User getUser(int uid) {
        // calculate the hash and retrive the head bucket from the table
        int hash = uid % tableCapacity;
        Bucket<User> bucket = table[hash];
        // iterate through list of buckets
        while (bucket != null) {
            if (bucket.user.getId() == uid) {
                return bucket.user;
            }
            bucket = bucket.next;
        }
        // no users found in the table with the given id.
        return null;
    }

    /**
     * getUsers: retrive and return all of the users from the table. The array
     * returned is sorted by recently joined users being at the start.
     *
     * Algorithm Complexity: O(nk) + O(n logn) = O(n logn)
     * where n is the number users in the table
     * where k is the number of buckets in list (On average is tableSize/tableCapacity)
     *
     */

    public User[] getUsers() {
        // create an empty array with size equal to num of users
        User[] users = new User[tableSize];
        int cur_index = 0;
        // iterate through the hash table, and then the list of buckets
        for (int i=0; i<tableCapacity; i++) {
            // get the head bucket at each table index
            Bucket<User> bucket = table[i];
            while (bucket != null) {
                // add the user to the array from each bucket
                users[cur_index++] = bucket.user;
                bucket = bucket.next;
            }
        }
        // sort the array by date joined and return it
        sort(users, tableSize);
        return users;
    }

    /**
     * getUsersContaining: return an array of users who's name conatins the
     * given query, sorted such that the most recently joined user is first.
     *
     * Algorithm Complexity: O(n logn) + O(n) + O(m) = O(n logn)
     * where n is the number users in the table
     * where m is the number of users' names matching the query
     *
     */

    public User[] getUsersContaining(String query) {
        // get all the users from the table in joinDate order
        User[] allUsers = getUsers();
        User[] tempUsers = new User[tableSize];
        int users_count = 0;
        // iterate through the users in allUsers, checking if they match the query
        for (int i=0; i<tableSize; i++) {
            String name = allUsers[i].getName().toLowerCase();
            if (name.contains(query.toLowerCase())) {
                tempUsers[users_count++] = allUsers[i];
            }
        }
        // remove the null elements from the array and return
        User[] users = new User[users_count];
        for (int j=0; j<users_count; j++) {
            users[j] = tempUsers[j];
        }
        return users;
    }

    /**
     * getUsersJoinedBefore: return an array of the users joined before the
     * given date, sorted such that the most recently joined user is first in
     * the list.
     *
     * Algorithm Complexity: O(n logn) + O(n) + O(r) = O(n logn)
     * where n is the number users in the table
     * where r is the number of users joined before dateBefore
     *
     */

    public User[] getUsersJoinedBefore(Date dateBefore) {
        // get all users in joinDate order
        User[] allUsers = getUsers();
        User[] tempUsers = new User[tableSize];
        int user_count = 0;
        // iterate through the users in allUsers, checking if they joined before dateBefore
        for (int i=0; i<tableSize; i++) {
            Date joinDate = allUsers[i].getDateJoined();
            if (joinDate.compareTo(dateBefore) < 0) {
                tempUsers[user_count++] = allUsers[i];
            }
        }
        // remove the null elements from the array and return
        User[] users = new User[user_count];
        for (int j=0; j<user_count; j++) {
            users[j] = tempUsers[j];
        }
        return users;
    }

    /**
     * Methods for the sorting algorithm used to sort the users by date joined
     *
     * Algorithm Complexity: O(n logn)
     * where n is the number of elements in the array being sorted
     *
     */

    private static void sort(User[] users, int arraySize) {
        // recursion base - arraySize == 1
        if (arraySize < 2) {
            return;
        }
        int middle = arraySize / 2;
        // create temp arrays
        User[] left = new User[middle];
        User[] right = new User[arraySize - middle];
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

    private static void merge(User[] users, User[] l, User[] r, int leftSize, int rightSize) {
        int i = 0, j = 0, k = 0;
        // compare users date joined
        while (i < leftSize && j < rightSize) {
            if (l[i].getDateJoined().compareTo(r[j].getDateJoined()) >= 0) {
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
