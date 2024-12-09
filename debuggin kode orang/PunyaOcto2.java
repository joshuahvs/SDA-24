import java.io.*;
import java.util.StringTokenizer;

public class PunyaOcto2 {

    static InputReader in = new InputReader(System.in);
    static PrintWriter out = new PrintWriter(System.out);
    // Participant class representing each participant
    static class Participant {
        int id;
        int points;
        int matches;
        Participant left, right;
        int height;

        public Participant(int id, int points) {
            this.id = id;
            this.points = points;
            this.matches = 0;
            this.left = null;
            this.right = null;
            this.height = 1;
        }
    }

    // AVL Tree for Participants
    static class ParticipantAVLTree {
        Participant root;

        // Insert participant into AVL Tree
        public Participant insert(Participant node, Participant participant) {
            if (node == null) {
                return participant;
            }

            int cmp = compareParticipants(participant, node);
            if (cmp < 0) {
                node.left = insert(node.left, participant);
            } else if (cmp > 0) {
                node.right = insert(node.right, participant);
            } else {
                // IDs are unique, so this case shouldn't happen
                return node;
            }

            node.height = 1 + Math.max(getHeight(node.left), getHeight(node.right));

            int balance = getBalance(node);

            // Left Left Case
            if (balance > 1 && compareParticipants(participant, node.left) < 0) {
                return rightRotate(node);
            }

            // Right Right Case
            if (balance < -1 && compareParticipants(participant, node.right) > 0) {
                return leftRotate(node);
            }

            // Left Right Case
            if (balance > 1 && compareParticipants(participant, node.left) > 0) {
                node.left = leftRotate(node.left);
                return rightRotate(node);
            }

            // Right Left Case
            if (balance < -1 && compareParticipants(participant, node.right) < 0) {
                node.right = rightRotate(node.right);
                return leftRotate(node);
            }

            return node;
        }

        // Delete participant from AVL Tree
        public Participant delete(Participant root, Participant participant) {
            if (root == null) {
                return root;
            }

            int cmp = compareParticipants(participant, root);
            if (cmp < 0) {
                root.left = delete(root.left, participant);
            } else if (cmp > 0) {
                root.right = delete(root.right, participant);
            } else {
                // This is the node to be deleted
                if ((root.left == null) || (root.right == null)) {
                    Participant temp = (root.left != null) ? root.left : root.right;

                    if (temp == null) {
                        // No child case
                        temp = root;
                        root = null;
                    } else {
                        // One child case
                        root = temp;
                    }
                } else {
                    // Node with two children
                    Participant temp = minValueNode(root.right);
                    // Copy the inorder successor's data to this node
                    root.id = temp.id;
                    root.points = temp.points;
                    root.matches = temp.matches;
                    // Delete the inorder successor
                    root.right = delete(root.right, temp);
                }
            }

            if (root == null) {
                return root;
            }

            root.height = Math.max(getHeight(root.left), getHeight(root.right)) + 1;

            int balance = getBalance(root);

            // Balancing the tree
            // Left Left Case
            if (balance > 1 && getBalance(root.left) >= 0) {
                return rightRotate(root);
            }

            // Left Right Case
            if (balance > 1 && getBalance(root.left) < 0) {
                root.left = leftRotate(root.left);
                return rightRotate(root);
            }

            // Right Right Case
            if (balance < -1 && getBalance(root.right) <= 0) {
                return leftRotate(root);
            }

            // Right Left Case
            if (balance < -1 && getBalance(root.right) > 0) {
                root.right = rightRotate(root.right);
                return leftRotate(root);
            }

            return root;
        }

        // Utility functions for AVL Tree
        int getHeight(Participant N) {
            if (N == null) {
                return 0;
            }
            return N.height;
        }

        int getBalance(Participant N) {
            if (N == null) {
                return 0;
            }
            return getHeight(N.left) - getHeight(N.right);
        }

        Participant rightRotate(Participant y) {
            Participant x = y.left;
            Participant T2 = x.right;

            // Perform rotation
            x.right = y;
            y.left = T2;

            // Update heights
            y.height = Math.max(getHeight(y.left), getHeight(y.right)) + 1;
            x.height = Math.max(getHeight(x.left), getHeight(x.right)) + 1;

            // Return new root
            return x;
        }

        Participant leftRotate(Participant x) {
            Participant y = x.right;
            Participant T2 = y.left;

            // Perform rotation
            y.left = x;
            x.right = T2;

            // Update heights
            x.height = Math.max(getHeight(x.left), getHeight(x.right)) + 1;
            y.height = Math.max(getHeight(y.left), getHeight(y.right)) + 1;

            // Return new root
            return y;
        }

        Participant minValueNode(Participant node) {
            Participant current = node;
            while (current.left != null) {
                current = current.left;
            }
            return current;
        }

        // Function to compare participants based on hierarchy
        int compareParticipants(Participant a, Participant b) {
            if (a.points != b.points) {
                return b.points - a.points; // Descending order
            } else if (a.matches != b.matches) {
                return a.matches - b.matches; // Ascending order
            } else {
                return a.id - b.id; // Ascending order
            }
        }

        // Function to find participant by ID
        Participant findParticipantById(Participant node, int id) {
            if (node == null) {
                return null;
            }
            if (node.id == id) {
                return node;
            }
            Participant res = findParticipantById(node.left, id);
            if (res != null) {
                return res;
            }
            return findParticipantById(node.right, id);
        }

        // Function to get the number of participants
        int getSize(Participant node) {
            if (node == null) {
                return 0;
            }
            return 1 + getSize(node.left) + getSize(node.right);
        }

        // In-order traversal to collect points for statistical calculations
        void inOrderCollectPoints(Participant node, int[] pointsArray, int[] index) {
            if (node != null) {
                inOrderCollectPoints(node.left, pointsArray, index);
                pointsArray[index[0]++] = node.points;
                inOrderCollectPoints(node.right, pointsArray, index);
            }
        }

        // Function to count participants with points greater than U or less than L
        int countExtremeParticipants(Participant root, double bound, boolean upper) {
            if (root == null) {
                return 0;
            }

            int count = 0;
            if (upper) {
                if (root.points > bound) {
                    count = 1;
                }
            } else {
                if (root.points < bound) {
                    count = 1;
                }
            }

            //System.out.println("Debug: Node points = " + root.points + ", Bound = " + bound + ", Count = " + count);

            return count + countExtremeParticipants(root.left, bound, upper)
                        + countExtremeParticipants(root.right, bound, upper);
        }


        // Function to count unique points
        void countUniquePoints(Participant node, java.util.HashSet<Integer> set) {
            if (node != null) {
                set.add(node.points);
                countUniquePoints(node.right, set);
                countUniquePoints(node.left, set);
            }
        }

        // Function to print participants in the tree
        public void printParticipants(Participant node) {
            if (node == null) {
                return;
            }

            // Traverse left subtree
            printParticipants(node.left);

            // Print current participant's details
            out.printf("ID: %d, Points: %d, Matches: %d%n", node.id, node.points, node.matches);

            // Traverse right subtree
            printParticipants(node.right);
        }

        // Function to print participants ordered by ID
        public void printParticipantsOrderedByID(Participant node) {
            if (node == null) {
                return;
            }

            // In-order traversal ensures participants are printed in ascending order of IDs
            printParticipantsOrderedByID(node.left);

            // Print current participant's details
            out.printf("ID: %d, Points: %d, Matches: %d%n", node.id, node.points, node.matches);

            printParticipantsOrderedByID(node.right);
        }



        // Function to collect top N participants (used when removing top 3)
        void collectTopNParticipants(Participant node, java.util.List<Participant> list, int N) {
            if (node == null || list.size() >= N) {
                return;
            }
            collectTopNParticipants(node.left, list, N);
            if (list.size() < N) {
                list.add(node);
            }
            collectTopNParticipants(node.right, list, N);
        }

        // Function to set all participants' points to 1
        void setAllParticipantsPoints(Participant node, Team team) {
            if (node != null) {
                setAllParticipantsPoints(node.left, team);
                team.totalPoints -= node.points;
                node.points = 1;
                team.totalPoints += node.points;
                setAllParticipantsPoints(node.right, team);
            }
        }
    }

    // Team class representing each team
    static class Team {
        int id;
        int totalPoints;
        int numParticipants;
        Team prev, next;
        ParticipantAVLTree participants;
        int timesCaught;
        int totalMatches;

        public Team(int id) {
            this.id = id;
            this.totalPoints = 0;
            this.numParticipants = 0;
            this.prev = null;
            this.next = null;
            this.participants = new ParticipantAVLTree();
            this.timesCaught = 0;
            this.totalMatches = 0;
        }
    }

    // Doubly Linked List for Teams
    static class TeamLinkedList {
        Team head;
        Team tail;
        int size;

        public TeamLinkedList() {
            this.head = null;
            this.tail = null;
            this.size = 0;
        }

        // Add team at the end
        public void addTeam(Team team) {
            if (head == null) {
                head = tail = team;
            } else {
                tail.next = team;
                team.prev = tail;
                tail = team;
            }
            size++;
        }

        // Insert team to the left or right of a given team
        public void insertTeam(Team current, Team newTeam, boolean toLeft) {
            if (toLeft) {
                Team prevTeam = current.prev;
                newTeam.next = current;
                newTeam.prev = prevTeam;
                current.prev = newTeam;
                if (prevTeam != null) {
                    prevTeam.next = newTeam;
                } else {
                    head = newTeam;
                }
            } else {
                Team nextTeam = current.next;
                newTeam.prev = current;
                newTeam.next = nextTeam;
                current.next = newTeam;
                if (nextTeam != null) {
                    nextTeam.prev = newTeam;
                } else {
                    tail = newTeam;
                }
            }
            size++;
        }

        // Remove a team
        public void removeTeam(Team team) {
            if (team.prev != null) {
                team.prev.next = team.next;
            } else {
                head = team.next;
            }
            if (team.next != null) {
                team.next.prev = team.prev;
            } else {
                tail = team.prev;
            }
            team.prev = null;
            team.next = null;
            size--;
        }

        // Get team to the left or right, considering wrapping
        public Team move(Team current, boolean toLeft) {
            if (toLeft) {
                if (current.prev != null) {
                    return current.prev;
                } else {
                    return tail;
                }
            } else {
                if (current.next != null) {
                    return current.next;
                } else {
                    return head;
                }
            }
        }

        // Collect all teams into an array for sorting
        public Team[] toArray() {
            Team[] array = new Team[size];
            int idx = 0;
            Team current = head;
            while (current != null) {
                array[idx++] = current;
                current = current.next;
            }
            return array;
        }

        // Rebuild linked list from sorted array
        public void rebuildFromArray(Team[] array) {
            head = array[0];
            head.prev = null;
            for (int i = 1; i < array.length; i++) {
                array[i - 1].next = array[i];
                array[i].prev = array[i - 1];
            }
            tail = array[array.length - 1];
            tail.next = null;
        }


        public void printCurrentState() {
            out.printf("%-10s | %-10s | %-15s | %-15s%n", "Index", "Team ID", "Participants", "Total Points");
            out.println("--------------------------------------------------------------");
        
            Team current = head;
            int index = 0;
        
            while (current != null) {
                out.printf("%-10d | %-10d | %-15d | %-15d%n",
                        index,
                        current.id,
                        current.numParticipants,
                        current.totalPoints);
                current = current.next;
                index++;
            }
        }
    }

    // Merge Sort implementation for sorting teams
    static class TeamSorter {
        public void mergeSort(Team[] array, int left, int right) {
            if (left < right) {
                int mid = (left + right) / 2;
                mergeSort(array, left, mid);
                mergeSort(array, mid + 1, right);
                merge(array, left, mid, right);
            }
        }

        public void merge(Team[] array, int left, int mid, int right) {
            int n1 = mid - left + 1;
            int n2 = right - mid;

            Team[] L = new Team[n1];
            Team[] R = new Team[n2];

            for (int i = 0; i < n1; i++) {
                L[i] = array[left + i];
            }
            for (int j = 0; j < n2; j++) {
                R[j] = array[mid + 1 + j];
            }

            int i = 0, j = 0;
            int k = left;

            while (i < n1 && j < n2) {
                if (compareTeams(L[i], R[j]) < 0) {
                    array[k++] = L[i++];
                } else {
                    array[k++] = R[j++];
                }
            }

            while (i < n1) {
                array[k++] = L[i++];
            }
            while (j < n2) {
                array[k++] = R[j++];
            }
        }

        // Compare teams based on totalPoints (descending), numParticipants (ascending),
        // id (ascending)
        int compareTeams(Team a, Team b) {
            if (a.totalPoints != b.totalPoints) {
                return b.totalPoints - a.totalPoints;
            } else if (a.numParticipants != b.numParticipants) {
                return a.numParticipants - b.numParticipants;
            } else {
                return a.id - b.id;
            }
        }
    }

    // Main function
    public static void main(String[] args) {

        int M = in.nextInteger();
        int totalParticipants = 0;
        TeamLinkedList teamList = new TeamLinkedList();
        java.util.HashMap<Integer, Team> teamMap = new java.util.HashMap<>();
        java.util.HashMap<Integer, Participant> participantMap = new java.util.HashMap<>();
        int participantIdCounter = 1;
        int teamId = 1;

        // Read number of participants per team
        int[] Mi = new int[M];
        for (int i = 0; i < M; i++) {
            Mi[i] = in.nextInteger();
            totalParticipants += Mi[i];
        }

        // Read initial points for each participant
        int[] initialPoints = new int[totalParticipants];
        for (int i = 0; i < totalParticipants; i++) {
            initialPoints[i] = in.nextInteger();
        }

        int pointIndex = 0;

        // Initialize teams and participants
        for (int i = 0; i < M; i++) {
            Team team = new Team(teamId++);
            int numParticipants = Mi[i];
            for (int j = 0; j < numParticipants; j++) {
                Participant participant = new Participant(participantIdCounter++, initialPoints[pointIndex++]);
                team.participants.root = team.participants.insert(team.participants.root, participant);
                team.totalPoints += participant.points;
                team.numParticipants++;
                participantMap.put(participant.id, participant);
            }
            teamList.addTeam(team);
            teamMap.put(team.id, team);
        }

        // Initialize Sofita and Penjoki
        Team sofitaTeam = teamList.head; // Sofita starts at the first team
        Team jockeyTeam = findTeamWithLowestPointsNotSupervised(teamList, sofitaTeam); // Penjoki starts at the team with lowest points
        // if (jockeyTeam == sofitaTeam) {
        //     jockeyTeam = findTeamWithSecondLowestPoints(teamList, sofitaTeam);
        // }

        int Q = in.nextInteger();

        TeamSorter sorter = new TeamSorter();

        for (int q = 0; q < Q; q++) {
            String command = in.next();
            if (command.equals("A")) {
                int jumlahPeserta = in.nextInteger();
                if (sofitaTeam == null) {
                    out.println(-1);
                } else {
                    for (int i = 0; i < jumlahPeserta; i++) {
                        Participant participant = new Participant(participantIdCounter++, 3);
                        sofitaTeam.participants.root = sofitaTeam.participants.insert(sofitaTeam.participants.root,
                                participant);
                        sofitaTeam.totalPoints += participant.points;
                        sofitaTeam.numParticipants++;
                        participantMap.put(participant.id, participant);
                    }
                    out.println(sofitaTeam.numParticipants);

                    // out.println(sofitaTeam.id);
                    // out.println(jockeyTeam.id);
                }
            } else if (command.equals("B")) {
                String extremeBound = in.next();
                if (sofitaTeam == null) {
                    // out.println("Debug: sofitaTeam is null");
                    out.println(0);
                } else {
                    int size = sofitaTeam.participants.getSize(sofitaTeam.participants.root);
                    // out.println("Debug: Size of participants = " + size);
                    
                    if (size == 0) {
                        out.println(0);
                        return;
                    }

                    int[] pointsArray = new int[size];
                    int[] idx = new int[] { 0 };
                    sofitaTeam.participants.inOrderCollectPoints(sofitaTeam.participants.root, pointsArray, idx);
                    // out.println("Debug: Points array before sort = " + java.util.Arrays.toString(pointsArray));

                    java.util.Arrays.sort(pointsArray);
                    // out.println("Debug: Points array after sort = " + java.util.Arrays.toString(pointsArray));

                    int K = size;
                    int indexQ1 = Math.max(0, (int) Math.floor((K - 1) / 4.0));
                    int indexQ3 = Math.min(K - 1, (int) Math.floor(3 * (K - 1) / 4.0));
                    int Q1 = pointsArray[indexQ1];
                    int Q3 = pointsArray[indexQ3];
                    int IQR = Q3 - Q1;
                    // out.println("Debug: Q1 = " + Q1 + ", Q3 = " + Q3 + ", IQR = " + IQR);

                    double L = Q1 - 1.5 * IQR;
                    double U = Q3 + 1.5 * IQR;
                    // out.println("Debug: Lower bound (L) = " + L + ", Upper bound (U) = " + U);

                    if (extremeBound.equals("U")) {
                        int count = sofitaTeam.participants.countExtremeParticipants(sofitaTeam.participants.root, U, true);
                        // out.println("Debug: Count of participants > U = " + count);
                        out.println(count);
                    } else if (extremeBound.equals("L")) {
                        int count = sofitaTeam.participants.countExtremeParticipants(sofitaTeam.participants.root, L, false);
                        // out.println("Debug: Count of participants < L = " + count);
                        out.println(count);
                    } else {
                        // System.out.println("Debug: Invalid extremeBound = " + extremeBound);
                        out.println(0);
                    }
                }
            } else if (command.equals("M")) {
                String direction = in.next();
                boolean printMinOne = false;
                if (sofitaTeam == null) {
                    printMinOne = true;
                } else {
                    // Print debug information
                    // out.println();
                    // teamList.printCurrentState();
                    // out.println("joki team = " + (jockeyTeam != null ? jockeyTeam.id : "null"));
                    // out.println("sofita team = " + sofitaTeam.id);
                    // out.println();
            
                    // Move sofitaTeam based on direction
                    if (direction.equals("L")) {
                        sofitaTeam = teamList.move(sofitaTeam, true); // Move left
                    } else {
                        sofitaTeam = teamList.move(sofitaTeam, false); // Move right
                    }
            
                    boolean jockeyTeamAssign = false;
                    while (teamList.size>0 && sofitaTeam == jockeyTeam){
                        // out.println("masuk sini 4");
                        // Check if sofitaTeam encounters jockeyTeam
                        if (sofitaTeam != null && jockeyTeam != null && sofitaTeam.id == jockeyTeam.id) {
                            sofitaTeam.timesCaught++;
                            if (sofitaTeam.timesCaught == 1) {
                                // out.println("masuk sini 5");
                                // First time caught: Remove top 3 participants
                                java.util.List<Participant> topParticipants = new java.util.ArrayList<>();
                                sofitaTeam.participants.collectTopNParticipants(sofitaTeam.participants.root, topParticipants, 3);
                                for (Participant p : topParticipants) {
                                    // out.println("participant to removed id = " + p.id + " part of team " + sofitaTeam.id);
                                    sofitaTeam.participants.root = sofitaTeam.participants.delete(sofitaTeam.participants.root, p);
                                    sofitaTeam.totalPoints -= p.points;
                                    sofitaTeam.numParticipants--;
                                    participantMap.remove(p.id);
                                }
                                // out.println("remove participant disini 1");
                                jockeyTeamAssign = true;
                            } else if (sofitaTeam.timesCaught == 2) {
                                // out.println("masuk sini 6");
                                // Second time caught: Set all points to 1
                                sofitaTeam.participants.setAllParticipantsPoints(sofitaTeam.participants.root, sofitaTeam);
                                jockeyTeamAssign = true;
                            } else if (sofitaTeam.timesCaught == 3) {
                                // out.println("masuk sini 7");
                                // Third time caught: Eliminate team
                                teamList.removeTeam(sofitaTeam);
                                teamMap.remove(sofitaTeam.id);
                                sofitaTeam = null;
                            }
                            // Check if sofitaTeam has fewer than 7 participants
                            if (sofitaTeam != null && sofitaTeam.numParticipants < 7) {
                                teamList.removeTeam(sofitaTeam);
                                teamMap.remove(sofitaTeam.id);
                                sofitaTeam = null;
                            }
                            
                            if (jockeyTeamAssign == true){
                                jockeyTeam = findTeamWithLowestPointsNotSupervised(teamList, sofitaTeam);
                            }

                            // Handle case when sofitaTeam becomes null
                            if (sofitaTeam == null) {
                                if (teamList.size == 0){
                                    // out.println("masuk sini 1");
                                    sofitaTeam = null;
                                    jockeyTeam = null;
                                    printMinOne = true;
                                } else if (teamList.size == 1){
                                    // out.println("masuk sini 2");;
                                    sofitaTeam = findTeamWithHighestPoints(teamList);
                                    jockeyTeam = sofitaTeam;
                                }else if (teamList.size>1){
                                    // out.println("masuk sini 3");;
                                    sofitaTeam = findTeamWithHighestPoints(teamList);
                                    jockeyTeam = findTeamWithLowestPointsNotSupervised(teamList, sofitaTeam);
                                }
                            }
                        } else{
                            printMinOne = true;
                        }
                    }
                }

                if (printMinOne == true){
                    out.println(-1);
                }else{
                    out.println(sofitaTeam.id);
                }

                // out.println();
                // teamList.printCurrentState();
                // out.println("joki team = " + (jockeyTeam != null ? jockeyTeam.id : "null"));
                // out.println("sofita team = " + (sofitaTeam != null ? sofitaTeam.id : "null"));
                // out.println();

            } else if (command.equals("T")) {
                int idPengirim = in.nextInteger();
                int idPenerima = in.nextInteger();
                int jumlahPoin = in.nextInteger();
                if (sofitaTeam == null) {
                    out.println(-1);
                } else {
                    Participant pengirim = participantMap.get(idPengirim);
                    Participant penerima = participantMap.get(idPenerima);

                    // out.println("sender's point before = " + pengirim.points);
                    // out.println("receiver's point before = " + penerima.points);

                    if (pengirim == null || penerima == null || pengirim.points <= jumlahPoin || pengirim == penerima) {
                        out.println(-1);
                    } else {
                        if (!isParticipantInTeam(pengirim, sofitaTeam) || !isParticipantInTeam(penerima, sofitaTeam)) {
                            out.println(-1);
                        } else {
                            // out.println("jumlah poin disini 1 = " + pengirim.points);
                            pengirim.points -= jumlahPoin;
                            // out.println("jumlah poin disini 2 = " + pengirim.points);
                            penerima.points += jumlahPoin;
                            pengirim.matches++;
                            // out.println("jumlah poin disini 3 = " + pengirim.points);
                            penerima.matches++;
                            // Update AVL Tree
                            // updateParticipantInTeam(sofitaTeam, pengirim);
                            // out.println("jumlah poin disini 4 = " + pengirim.points);
                            // updateParticipantInTeam(sofitaTeam, penerima);
                            // Total points remain the same
                            // out.println("jumlah poin disini 5 = " + pengirim.points);
                            out.println(pengirim.points + " " + penerima.points);
                            // Remove participants with zero or negative points
                            if (pengirim.points <= 0) {
                                sofitaTeam.participants.root = sofitaTeam.participants
                                        .delete(sofitaTeam.participants.root, pengirim);
                                sofitaTeam.numParticipants--;
                                participantMap.remove(pengirim.id);
                                // Update total points
                                sofitaTeam.totalPoints -= pengirim.points;
                                // out.println("remove participant disini 2");
                            }
                            // Check if team needs to be eliminated
                            if (sofitaTeam.numParticipants < 7) {
                                teamList.removeTeam(sofitaTeam);
                                teamMap.remove(sofitaTeam.id);
                                if (sofitaTeam == jockeyTeam) {
                                    jockeyTeam = null;
                                }
                                sofitaTeam = null;
                                if (teamList.size > 0) {
                                    sofitaTeam = findTeamWithHighestPoints(teamList);
                                }
                            }
                        }
                        // out.println("sender's point after = " + pengirim.points);
                        // out.println("receiver's point after = " + penerima.points);

                    }
                }
            } else if (command.equals("G")) {
                String direction = in.next();
                Team newTeam = new Team(teamId++);
                for (int i = 0; i < 7; i++) {
                    Participant participant = new Participant(participantIdCounter++, 1);
                    newTeam.participants.root = newTeam.participants.insert(newTeam.participants.root, participant);
                    newTeam.totalPoints += participant.points;
                    newTeam.numParticipants++;
                    participantMap.put(participant.id, participant);
                }
                if (sofitaTeam == null) {
                    teamList.addTeam(newTeam);
                } else {
                    if (direction.equals("L")) {
                        teamList.insertTeam(sofitaTeam, newTeam, true);
                    } else {
                        teamList.insertTeam(sofitaTeam, newTeam, false);
                    }
                }
                teamMap.put(newTeam.id, newTeam);
                out.println(newTeam.id);
            } else if (command.equals("V")) {
                int idPeserta1 = in.nextInteger();
                int idPeserta2 = in.nextInteger();
                int idTim = in.nextInteger();
                int hasil = in.nextInteger();
                Participant peserta1 = participantMap.get(idPeserta1);
                Participant peserta2 = participantMap.get(idPeserta2);
                Team team2 = teamMap.get(idTim);
                if (sofitaTeam == null || peserta1 == null || peserta2 == null || team2 == null) {
                    out.println(-1);
                } else {
                    if (!isParticipantInTeam(peserta1, sofitaTeam) || !isParticipantInTeam(peserta2, team2)) {
                        out.println(-1);
                    } else {
                        if (hasil == 0) {
                            peserta1.points += 1;
                            peserta2.points += 1;
                            peserta1.matches++;
                            peserta2.matches++;
                            // Update AVL Trees
                            // updateParticipantInTeam(sofitaTeam, peserta1);
                            // updateParticipantInTeam(team2, peserta2);
                            out.println(peserta1.points + " " + peserta2.points);
                        } else if (hasil == 1) {
                            peserta1.points += 3;
                            peserta2.points -= 3;
                            peserta1.matches++;
                            peserta2.matches++;
                            // updateParticipantInTeam(sofitaTeam, peserta1);
                            if (peserta2.points <= 0) {
                                team2.participants.root = team2.participants.delete(team2.participants.root, peserta2);
                                team2.numParticipants--;
                                participantMap.remove(peserta2.id);
                                // Update total points
                                team2.totalPoints -= peserta2.points;
                                // out.println("remove participant disini 3");
                            } else {
                                // updateParticipantInTeam(team2, peserta2);
                                team2.totalPoints -= 3;
                            }
                            out.println(peserta1.points);
                            // Update total points
                            sofitaTeam.totalPoints += 3;
                            // Check for team elimination
                            if (team2.numParticipants < 7) {
                                teamList.removeTeam(team2);
                                teamMap.remove(team2.id);
                                if (team2 == jockeyTeam) {
                                    jockeyTeam = null;
                                }
                            }
                        } else if (hasil == -1) {
                            peserta2.points += 3;
                            peserta1.points -= 3;
                            peserta1.matches++;
                            peserta2.matches++;
                            // updateParticipantInTeam(team2, peserta2);
                            if (peserta1.points <= 0) {
                                sofitaTeam.participants.root = sofitaTeam.participants
                                        .delete(sofitaTeam.participants.root, peserta1);
                                sofitaTeam.numParticipants--;
                                participantMap.remove(peserta1.id);
                                // Update total points
                                sofitaTeam.totalPoints -= peserta1.points;
                                // out.println("remove participant disini 4");
                            } else {
                                // updateParticipantInTeam(sofitaTeam, peserta1);
                                sofitaTeam.totalPoints -= 3;
                            }
                            out.println(peserta2.points);
                            // Update total points
                            team2.totalPoints += 3;
                            // Check for team elimination
                            if (sofitaTeam.numParticipants < 7) {
                                teamList.removeTeam(sofitaTeam);
                                teamMap.remove(sofitaTeam.id);
                                if (sofitaTeam == jockeyTeam) {
                                    jockeyTeam = null;
                                }
                                sofitaTeam = null;
                                if (teamList.size > 0) {
                                    sofitaTeam = findTeamWithHighestPoints(teamList);
                                }
                            }
                        }
                    }
                }
            } else if (command.equals("E")) {
                int poin = in.nextInteger();
                java.util.List<Team> eliminatedTeams = new java.util.ArrayList<>();
                Team current = teamList.head;
                while (current != null) {
                    if (current.totalPoints < poin) {
                        eliminatedTeams.add(current);
                    }
                    current = current.next;
                }
                for (Team team : eliminatedTeams) {
                    if (team == sofitaTeam) {
                        sofitaTeam = null;
                    }
                    if (team == jockeyTeam) {
                        jockeyTeam = null;
                    }
                    teamList.removeTeam(team);
                    teamMap.remove(team.id);
                }
                if (sofitaTeam == null && teamList.size > 0) {
                    sofitaTeam = findTeamWithHighestPoints(teamList);
                }
                out.println(eliminatedTeams.size());
            } else if (command.equals("U")) {
                if (sofitaTeam == null) {
                    out.println(-1);
                } else {
                    // out.println("total participants = " + sofitaTeam.numParticipants);
                    // out.println("tim id = " + sofitaTeam.id);
                    // out.println("total points = " + sofitaTeam.totalPoints);

                    java.util.HashSet<Integer> uniquePoints = new java.util.HashSet<>();

                    // Debug: Print all participants ordered by ID
                    // out.println("Participants in Sofita's Team (Ordered by ID):");
                    // sofitaTeam.participants.printParticipantsOrderedByID(sofitaTeam.participants.root);

                    // Count unique points
                    sofitaTeam.participants.countUniquePoints(sofitaTeam.participants.root, uniquePoints);
                    out.println(uniquePoints.size());

                    // Debug output for unique points
                    // out.println("Unique Points: " + uniquePoints);
                }
            } else if (command.equals("R")) {
                Team[] teamsArray = teamList.toArray();
                sorter.mergeSort(teamsArray, 0, teamsArray.length - 1);
                teamList.rebuildFromArray(teamsArray);

                // After sorting, Sofita supervises the team with the highest points
                if (teamList.size > 0) {
                    sofitaTeam = teamList.head; // Team with highest points
                    // Update jockeyTeam to the new team object after sorting
                    if (jockeyTeam != null) {
                        jockeyTeam = teamMap.get(jockeyTeam.id);
                    }

                    // Check for Penjoki
                    if (sofitaTeam == jockeyTeam) {
                        // Apply consequences
                        sofitaTeam.timesCaught++;
                        if (sofitaTeam.timesCaught == 1) {
                            // First time: Remove top 3 participants
                            java.util.List<Participant> topParticipants = new java.util.ArrayList<>();
                            sofitaTeam.participants.collectTopNParticipants(sofitaTeam.participants.root,
                                    topParticipants, 3);
                            for (Participant p : topParticipants) {
                                sofitaTeam.participants.root = sofitaTeam.participants
                                        .delete(sofitaTeam.participants.root, p);
                                sofitaTeam.totalPoints -= p.points;
                                sofitaTeam.numParticipants--;
                                participantMap.remove(p.id);
                                // out.println("remove participant disini 5");
                            }
                        } else if (sofitaTeam.timesCaught == 2) {
                            // Second time: Set all points to 1
                            sofitaTeam.participants.setAllParticipantsPoints(sofitaTeam.participants.root, sofitaTeam);
                        } else if (sofitaTeam.timesCaught == 3) {
                            // Third time: Eliminate team
                            teamList.removeTeam(sofitaTeam);
                            teamMap.remove(sofitaTeam.id);
                            if (sofitaTeam == jockeyTeam) {
                                jockeyTeam = null;
                            }
                            sofitaTeam = null;
                            if (teamList.size > 0) {
                                sofitaTeam = findTeamWithHighestPoints(teamList);
                            }
                        }
                        // After consequences, check if team has less than 7 participants
                        if (sofitaTeam != null && sofitaTeam.numParticipants < 7) {
                            teamList.removeTeam(sofitaTeam);
                            teamMap.remove(sofitaTeam.id);
                            if (sofitaTeam == jockeyTeam) {
                                jockeyTeam = null;
                            }
                            sofitaTeam = null;
                            if (teamList.size > 0) {
                                sofitaTeam = findTeamWithHighestPoints(teamList);
                                // Move Penjoki to team with lowest points not supervised by Sofita
                                jockeyTeam = findTeamWithLowestPointsNotSupervised(teamList, sofitaTeam);
                            }
                        }
                    }
                } else {
                    sofitaTeam = null;
                    jockeyTeam = null;
                }
                if (sofitaTeam == null) {
                    out.println(-1);
                } else {
                    out.println(sofitaTeam.id);
                }

                // printTeamList();
                // out.println("sofita team sesudah R = " + sofitaTeam.id);
                // out.println("joki team sesudah R = " + jockeyTeam.id);
                // out.println();
            } else if (command.equals("J")) {
                String direction = in.next();
                if (jockeyTeam == null) {
                    out.println(-1);
                } else {
                    Team nextTeam = teamList.move(jockeyTeam, direction.equals("L"));
                    if (nextTeam != sofitaTeam) {
                        jockeyTeam = nextTeam;
                    }
                    out.println(jockeyTeam.id);

                    // out.println("sofitaTeam after J = "+ sofitaTeam.id);
                    // out.println("jockey team after J = "+jockeyTeam.id);
                }
            }
        }

        out.flush();
    }

    static boolean isParticipantInTeam(Participant participant, Team team) {
        return team.participants.findParticipantById(team.participants.root, participant.id) != null;
    }

    static void updateParticipantInTeam(Team team, Participant participant) {
        team.participants.root = team.participants.delete(team.participants.root, participant);
        team.participants.root = team.participants.insert(team.participants.root, participant);
    }

    // static Team findTeamWithLowestPoints(TeamLinkedList teamList) {
    //     Team current = teamList.head;
    //     Team minTeam = current;
    //     while (current != null) {
    //         if (current.totalPoints < minTeam.totalPoints) {
    //             minTeam = current;
    //         }
    //         current = current.next;
    //     }
    //     return minTeam;
    // }

    // static Team findTeamWithSecondLowestPoints(TeamLinkedList teamList, Team excludeTeam) {
    //     Team current = teamList.head;
    //     Team minTeam = null;
    //     int minPoints = Integer.MAX_VALUE;
    //     while (current != null) {
    //         if (current != excludeTeam && current.totalPoints < minPoints) {
    //             minPoints = current.totalPoints;
    //             minTeam = current;
    //         }
    //         current = current.next;
    //     }
    //     return minTeam;
    // }

    static Team findTeamWithLowestPointsNotSupervised(TeamLinkedList teamList, Team excludeTeam) {
        Team current = teamList.head;
        Team bestTeam = null;
    
        while (current != null) {
            // Skip the excluded team
            if (current == excludeTeam) {
                current = current.next;
                continue;
            }
    
            // If no team is selected yet, initialize the first valid team
            if (bestTeam == null) {
                bestTeam = current;
            } else {
                // Compare points first
                if (current.totalPoints < bestTeam.totalPoints) {
                    bestTeam = current;
                } 
                // If points are the same, compare the number of participants
                else if (current.totalPoints == bestTeam.totalPoints) {
                    if (current.numParticipants > bestTeam.numParticipants) {
                        bestTeam = current;
                    } 
                    // If participants are also equal, compare IDs
                    else if (current.numParticipants == bestTeam.numParticipants) {
                        if (current.id > bestTeam.id) {
                            bestTeam = current;
                        }
                    }
                }
            }
    
            current = current.next;
        }
        return bestTeam;
    }    

    static Team findTeamWithHighestPoints(TeamLinkedList teamList) {
        Team current = teamList.head;
        Team maxTeam = current;
        while (current != null) {
            if (current.totalPoints > maxTeam.totalPoints) {
                maxTeam = current;
            }
            current = current.next;
        }
        return maxTeam;
    }

    // InputReader class
    static class InputReader {
        public BufferedReader reader;
        public StringTokenizer tokenizer;

        public InputReader(InputStream stream) {
            reader = new BufferedReader(new InputStreamReader(stream), 32768);
            tokenizer = null;
        }

        public String next() {
            while (tokenizer == null || !tokenizer.hasMoreTokens()) {
                try {
                    String line = reader.readLine();
                    if (line == null) {
                        return null;
                    }
                    tokenizer = new StringTokenizer(line);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            return tokenizer.nextToken();
        }

        public int nextInteger() {
            return Integer.parseInt(next());
        }
    }
}
