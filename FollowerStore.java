/**
 * I decided to use a graph as the data structure for modelling the follow
 * relationships. I did this by using an adjacency matrix, as a 2d array. This
 * works by giving each user a position bases on their id in the matrix, a index
 * in the 2d array. This works well as the actual id of the id does not matter
 * but instead, the order that they were added to the matrix does. This is
 * impliemented by having an array of the user ids in the order that the users
 * are in the matrix. This means it takes O(n) time to find the users matrix
 * index and then from there it only takes O(1) time to find any relation another
 * user may have. This make finding different relationships between users very
 * simple and quick.
 *
 * As I stated in the other files, I am prioritising speed. Because of this, I
 * create a large 2d array initially as if they array fills up, then it has to
 * be resized, wasting precious resources and time. I would rather require more
 * storage than need more computing power as the data structure is not
 * impliemented in a way that makes it easy to get the relationships from.
 *
 * The way that the matrix works is by storing the date that the follow
 * relationship occurred on. If the element is null, then there is no
 * relationsip. This makes it very easy to check who follows who. By looking
 * down a column in the matrix, you can see the follows of the user at the
 * top of the column. Similary, by looking across a row, you can easily see the
 * users they the user in the left of the row follows. This make counting follows
 * and looking a mutual relationships very easy.
 *
 *
 * 
 */

package uk.ac.warwick.java.cs126.services;

import uk.ac.warwick.java.cs126.models.Weet;
import uk.ac.warwick.java.cs126.models.User;

import java.util.Date;


public class FollowerStore implements IFollowerStore {

    /**
     * SortingObject: Class used to sort the relationships by date
     *
     */
    private class SortingObject {
        // SortingObject stores the user id and the date the follow started
        public int uid;
        public Date date;

        public SortingObject(int uid, Date date) {
            this.uid = uid;
            this.date = date;
        }
    }
    // matrix stores the relationsips between all of the users added to it
    private Date[][] matrix;
    // indexStore keeps track of the users index in the matrix
    private int[] indexStore;
    // both keep track of the size of the matrix for when it needs to be resized
    private int matrixCapacity;
    private int matrixSize;

    public FollowerStore() {
        matrixCapacity = 1000;
        matrix = new Date[matrixCapacity][matrixCapacity];
        indexStore = new int[matrixCapacity];
        matrixSize = 0;
    }

    /**
     * addFollower: Add a follower relationsip to the data store. The relationsip
     * added is that uid1 follows uid2 on the date followDate. The method returns
     * true if successful (uid1 didnt follow uid2 already) and false if unsuccessful
     * (uid1 already followed uid2). Return false if the user tries to follow
     * themselves.
     *
     * Algorithm Complexity: O(n)
     * where n is the number of users in the matrix
     *
     */

    public boolean addFollower(int uid1, int uid2, Date followDate) {
        // make sure the user if not trying to follow themselves
        if (uid1 == uid2) {
            return false;
        }
        // get the index of the user in the adjacency matrix
        int uid1Index = getUserIndex(uid1);
        int uid2Index = getUserIndex(uid2);
        // check if uid1 follows uid2
        if (matrix[uid1Index][uid2Index] == null) {
            // add the date of the follow relationship to the adjacency matrix and return true
            matrix[uid1Index][uid2Index] = followDate;
            return true;
        }
        // the uid1 already follows uid2 so return false
        return false;
    }

    /**
     * getFollowers: returns an array of all users that follow the user with the
     * given id. The list is sorted such that the most recent followe is first
     * in the list.
     *
     * Algorithm Complexity: O(n) + O(n) + O(m) + O(m) = O(n)
     * where n is the number of users in the matrix
     * where m is the number of users who follow the given user
     *
     */

    public int[] getFollowers(int uid) {
        // get the index of the user in the adjacency matrix
        int uidIndex = getUserIndex(uid);
        // create array to hold the unsorted data
        SortingObject[] tempFollowers = new SortingObject[matrixSize];
        int follower_count = 0;
        // look down the users column in the adjacency matrix
        for (int i=0; i<matrixSize; i++) {
            if (matrix[i][uidIndex] != null) {
                tempFollowers[follower_count++] = new SortingObject(indexStore[i], matrix[i][uidIndex]);
            }
        }
        // remove the null elements from the end of the tempFollowers array
        SortingObject[] unsortedFollowers = new SortingObject[follower_count];
        for (int j=0; j<follower_count; j++) {
            unsortedFollowers[j] = tempFollowers[j];
        }
        // sort the array by date followed
        sort(unsortedFollowers, follower_count);
        // get the uid from the sorted array and return
        int[] followers = new int[follower_count];
        for (int k=0; k<follower_count; k++) {
            followers[k] = unsortedFollowers[k].uid;
        }
        return followers;
    }

    /**
     * getFollows: returns an array of all the user that the given user follows.
     * The array is sorted such that the most recent follow is first.
     *
     * Algorithm Complexity: O(n) + O(n) + O(m) + O(m) = O(n)
     * where n is the number of users in the matrix
     * where m is the number of users the given user follows
     *
     */

    public int[] getFollows(int uid) {
        // get the index of the user in the adjacency matrix
        int uidIndex = getUserIndex(uid);
        // create array to hold the unsorted data
        SortingObject[] tempFollows = new SortingObject[matrixSize];
        int follow_count = 0;
        // populate the tempFollows with user uid the given user follows
        for (int i=0; i<matrixSize; i++) {
            if (matrix[uidIndex][i] != null) {
                SortingObject object = new SortingObject(indexStore[i], matrix[uidIndex][i]);
                tempFollows[follow_count++] = object;
            }
        }
        // remove the null element from the end of the tempFollows
        SortingObject[] unsortedFollows = new SortingObject[follow_count];
        for (int j=0; j<follow_count; j++) {
            unsortedFollows[j] = tempFollows[j];
        }
        // sort the array by date followed
        sort(unsortedFollows, follow_count);
        // get the uid from the sorted array and return
        int[] follows = new int[follow_count];
        for (int k=0; k<follow_count; k++) {
            follows[k] = unsortedFollows[k].uid;
        }
        return follows;
    }

    /**
     * isAFollower: return true uf tge user with uidFollower is a follower of
     * the user with the id uidFollows. Returns false otherwise.
     *
     * Algorithm Complexity: O(n) + O(n) = O(n)
     * where n is the number of users in the matrix
     *
     */

    public boolean isAFollower(int uidFollower, int uidFollows) {
        // get the index of the users in the adjacency matrix
        int uid1Index = getUserIndex(uidFollower);
        int uid2Index = getUserIndex(uidFollows);
        // check if there is an existing follow relationship between the users
        if (matrix[uid1Index][uid2Index] != null) {
            return true;
        }
        return false;
    }

    /**
     * getNumFollowers: returns the number of followers that the user with given
     * id has.
     *
     * Algorithm Complexity: O(n) + O(n) = O(n)
     * where n is the number of users in the matrix
     *
     */

    public int getNumFollowers(int uid) {
        // get the index of the user in the adjacency matrix
        int uidIndex = getUserIndex(uid);
        int followers = 0;
        // iterate down the users column in the adjacency matrix
        for (int i=0; i<matrixSize; i++) {
            // increase followers by one when the element isnt null
            if (matrix[i][uidIndex] != null) {
                followers++;
            }
        }
        return followers;
    }

    /**
     * getMutualFollowers: return an array of the ids of all the users that follow
     * both of the given users. The list is sorted such that the most recently
     * follower is first.
     *
     * Algorithm Complexity: O(n) + O(n) + O(n) + O(m) + O(m) = O(n)
     * where n is the number of users in the matrix
     * where m is the number of users that follow both the given users
     *
     */

    public int[] getMutualFollowers(int uid1, int uid2) {
        // get the index of the user in the adjacency matrix
        int uid1Index = getUserIndex(uid1);
        int uid2Index = getUserIndex(uid2);
        SortingObject[] tempMutualFollowers = new SortingObject[matrixSize];
        int follower_count = 0;
        // iterate down both the column for uid1 and uid2
        for (int i=0; i<matrixSize; i++) {
            // if there is a follow relationship for both users, add user to temp array
            Date date1 = matrix[i][uid1Index];
            Date date2 = matrix[i][uid2Index];
            if (date1 != null && date2 != null) {
                // add the follow relationship that occurred more recently
                if (date1.before(date2) || date1.equals(date2)) {
                    tempMutualFollowers[follower_count++] = new SortingObject(indexStore[i], date1);
                } else {
                    tempMutualFollowers[follower_count++] = new SortingObject(indexStore[i], date2);
                }
            }
        }
        // remove the null element from the end of the temop array
        SortingObject[] unsortedMutualFollowers = new SortingObject[follower_count];
        for (int j=0; j<follower_count; j++) {
            unsortedMutualFollowers[j] = tempMutualFollowers[j];
        }
        // sort the array by date followed
        sort(unsortedMutualFollowers, follower_count);
        // get the uid from the sorted array and return
        int[] mutualFollowers = new int[follower_count];
        for (int k=0; k<follower_count; k++) {
            mutualFollowers[k] = unsortedMutualFollowers[k].uid;
        }
        return mutualFollowers;
    }

    /**
     * getMutualFollows: return an array of the all the user ids that are followed
     * by both given users. The list is sorted such that the most recent follow
     * is first.
     *
     * Algorithm Complexity: O(n) + O(n) + O(n) + O(m) + O(m) = O(n)
     * where n is the number of users in the matrix
     * where m is the number of users both the given users follow
     *
     */

    public int[] getMutualFollows(int uid1, int uid2) {
        // get the index of the user in the adjacency matrix
        int uid1Index = getUserIndex(uid1);
        int uid2Index = getUserIndex(uid2);
        SortingObject[] tempMutualFollows = new SortingObject[matrixSize];
        int follow_count = 0;
        // iterate down both the column for uid1 and uid2
        for (int i=0; i<matrixSize; i++) {
            // if there is a follow relationship for both users, add user to temp array
            Date date1 = matrix[uid1Index][i];
            Date date2 = matrix[uid2Index][i];
            if (date1 != null && date2 != null) {
                // add the follow relationship that occurred more recently
                if (date1.before(date2) || date1.equals(date2)) {
                    tempMutualFollows[follow_count++] = new SortingObject(indexStore[i], date1);
                } else {
                    tempMutualFollows[follow_count++] = new SortingObject(indexStore[i], date2);
                }
            }
        }
        // remove the null element from the end of the temp array
        SortingObject[] unsortedMutualFollows = new SortingObject[follow_count];
        for (int j=0; j<follow_count; j++) {
            unsortedMutualFollows[j] = tempMutualFollows[j];
        }
        // sort the array by date followed
        sort(unsortedMutualFollows, follow_count);
        // get the uid from the sorted array and return
        int[] mutualFollows = new int[follow_count];
        for (int k=0; k<follow_count; k++) {
            mutualFollows[k] = unsortedMutualFollows[k].uid;
        }
        return mutualFollows;
    }

    /**
     * getTopUsers: return an array of the user ids such that the user with the
     * most followers is at the top.
     *
     * Algorithm Complexity: O(n) + O(n) + O(n) + O(m) + O(m) = O(n)
     * where n is the number of users in the matrix
     * where m is the number of user in topUsers
     */

    public int[] getTopUsers() {
        // array to store top 10 most followed users
        int[] topCount = new int[10];
        int[] topUsers = new int[10];
        // get the top 10 users
        for (int i=0; i<matrixSize; i++) {
            int followers = getNumFollowers(indexStore[i]);
            for (int j=0; j<10; j++) {
                if (followers >= topCount[j]) {
                    int[] tempCount = new int[10];
                    int[] tempUsers = new int[10];
                    for (int m=0; m<j; m++) {
                        tempCount[m] = topCount[m];
                        tempUsers[m] = topUsers[m];
                    }
                    tempCount[j] = followers;
                    tempUsers[j] = indexStore[i];
                    for (int k=j+1; k<10; k++) {
                        tempCount[k] = topCount[k-1];
                        tempUsers[k] = topUsers[k-1];
                    }
                    topCount = tempCount;
                    topUsers = tempUsers;
                    break;
                }
                // } else if (followers == topCount[j]) { //TODO: TEST THIS
                //     int[] tempCount = new int[10];
                //     int[] tempUsers = new int[10];
                //     for (int m=0; m<j+1; m++) {
                //         tempCount[m] = topCount[m];
                //         tempUsers[m] = topUsers[m];
                //     }
                //     tempCount[j+1] = followers;
                //     tempUsers[j+1] = indexStore[i];
                //     for (int k=j+2; k<10; k++) {
                //         tempCount[k] = topCount[k-1];
                //         tempUsers[k] = topUsers[k-1];
                //     }
                //     topCount = tempCount;
                //     topUsers = tempUsers;
                //     break;
                // }
            }
        }
        return topUsers;
    }

    /**
     * getUserIndex: given a user's id, get their position in the matrix. This
     * achieved by getting the position of the user in the array indexStore. If
     * a user is not in the store, this is where they will added. If the matrix
     * has reached capacity then the method ___ will be called before the new
     * is created.
     *
     * Algorithm Complexity: O(n) (O(n^2) worst case if matrix needs resizing)
     * where n is the number of users in the matrix
     *
     */

    // given user id, get the index of the user
    private int getUserIndex(int uid) {
        // see if the user is already in the adjacency matrix, returning their index if so
        for (int i=0; i<matrixSize; i++) {
            if (indexStore[i] == uid) {
                return i;
            }
        }
        // if user doesn't exist, add to indexStore
        // check the matrix isn't at capacity
        if (matrixSize == matrixCapacity) {
            increaseMatrix();
        }
        indexStore[matrixSize] = uid;
        return matrixSize++;
    }

    /**
     * increaseMatrix: double the size of the matrix and the indexStore so that
     * more users can be added to the store
     *
     * Algorithm Complexity: O(n^2) + O(n) = O(n^2)
     * where n is number of users in the matrix
     *
     */

    private void increaseMatrix() {
        // create tempery arrays to hold the data
        int newCapacity = matrixCapacity * 2;
        Date[][] tempMatrix = new Date[newCapacity][newCapacity];
        int[] tempIndexStore =  new int[newCapacity];
        // copy data to new arrays
        for (int i=0; i<matrixCapacity; i++) {
            for (int j=0; j<matrixCapacity; j++) {
                tempMatrix[i][j] = matrix[i][j];
            }
        }
        for (int k=0; k<matrixCapacity; k++) {
            tempIndexStore[k] = indexStore[k];
        }
        // set the new values
        matrix = tempMatrix;
        indexStore = tempIndexStore;
        matrixCapacity = newCapacity;
    }


    /**
     * Methods for the sorting algorithm used to sort the follow relationships
     * by date
     *
     * Algorithm Complexity: O(n logn)
     * where n is the number of elements in the array being sorted
     *
     */

    private static void sort(SortingObject[] followers, int arraySize) {
        // recursion base - arraySize == 1
        if (arraySize < 2) {
            return;
        }
        int middle = arraySize / 2;
        // create temp arrays
        SortingObject[] left = new SortingObject[middle];
        SortingObject[] right = new SortingObject[arraySize - middle];
        // copy the users array to temp arrays
        for (int i=0; i<middle; i++) {
            left[i] = followers[i];
        }
        for (int j=middle; j<arraySize; j++) {
            right[j - middle] = followers[j];
        }
        // recursive call
        sort(left, middle);
        sort(right, arraySize - middle);
        // merge the sub arrays
        merge(followers, left, right, middle, arraySize - middle);
    }

    public static void merge(SortingObject[] followers, SortingObject[] left, SortingObject[] right, int leftSize, int rightSize) {
        int i = 0, j = 0, k = 0;
        // compare follow date
        while (i < leftSize && j < rightSize) {
            if (left[i].date.after(right[j].date) || left[i].date.equals(right[j].date)) {
                followers[k++] = left[i++];
            } else {
                followers[k++] = right[j++];
            }
        }
        while (i < leftSize) {
            followers[k++] = left[i++];
        }
        while (j < rightSize) {
            followers[k++] = right[j++];
        }
    }
}
