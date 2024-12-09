import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.*;

public class TP2test5 {
    private static PrintWriter out;

    static Map<Integer, Participant> participants;
    static Map<Integer, Team> teams;
    static LinkedList<Team> teamList;
    static AVLTree<Team> teamTree;
    static Set<Integer> eliminatedParticipants;
    static Comparator<Participant> participantComparator;
    static Comparator<Team> teamComparator;

    static class Participant {
        int id;
        int points;
        int matches;
        Team team;

        public Participant(int id, int points, Team team) {
            this.id = id;
            this.points = points;
            this.matches = 0;
            this.team = team;
        }
    }

    static class Team implements Comparable<Team> {
        int id;
        AVLTree<Participant> participants;
        int totalPoints;
        int memberCount;
        int uniquePointCount;
        int[] pointCounts;
        int timesCaught;

        public Team(int id, Comparator<Participant> participantComparator) {
            this.id = id;
            this.participants = new AVLTree<>(participantComparator);
            this.totalPoints = 0;
            this.memberCount = 0;
            this.uniquePointCount = 0;
            this.pointCounts = new int[1001];
            this.timesCaught = 0;
        }

        @Override
        public int compareTo(Team other) {
            if (this.totalPoints != other.totalPoints) {
                return other.totalPoints - this.totalPoints; // Descending totalPoints
            } else if (this.memberCount != other.memberCount) {
                return this.memberCount - other.memberCount; // Ascending memberCount
            } else {
                return this.id - other.id; // Ascending id
            }
        }
    }

    // Self-implemented LinkedList for Teams
    static class LinkedList<T> implements Iterable<T> {
        class Node {
            T data;
            Node next;
            Node prev;

            public Node(T data) {
                this.data = data;
            }
        }

        Node head;
        Node tail;
        int size;

        public LinkedList() {
            head = null;
            tail = null;
            size = 0;
        }

        public void add(T data) {
            Node node = new Node(data);
            if (head == null) {
                head = tail = node;
            } else {
                tail.next = node;
                node.prev = tail;
                tail = node;
            }
            size++;
        }

        public void add(int index, T data) {
            if (index < 0 || index > size)
                return;
            if (index == 0) {
                addFirst(data);
                return;
            }
            if (index == size) {
                add(data);
                return;
            }
            Node newNode = new Node(data);
            Node current = head;
            int idx = 0;
            while (current != null) {
                if (idx == index) {
                    newNode.prev = current.prev;
                    newNode.next = current;
                    current.prev.next = newNode;
                    current.prev = newNode;
                    size++;
                    return;
                }
                current = current.next;
                idx++;
            }
        }

        public void addFirst(T data) {
            Node node = new Node(data);
            if (head == null) {
                head = tail = node;
            } else {
                node.next = head;
                head.prev = node;
                head = node;
            }
            size++;
        }

        public void remove(T data) {
            Node current = head;
            while (current != null) {
                if (current.data.equals(data)) {
                    removeNode(current);
                    break;
                }
                current = current.next;
            }
        }

        private void removeNode(Node node) {
            if (node.prev == null) {
                // Node is head
                head = node.next;
            } else {
                node.prev.next = node.next;
            }
            if (node.next == null) {
                // Node is tail
                tail = node.prev;
            } else {
                node.next.prev = node.prev;
            }
            size--;
        }

        public int indexOf(T data) {
            Node current = head;
            int index = 0;
            while (current != null) {
                if (current.data.equals(data))
                    return index;
                current = current.next;
                index++;
            }
            return -1;
        }

        public T get(int index) {
            if (index < 0 || index >= size)
                return null;
            Node current = head;
            int idx = 0;
            while (current != null) {
                if (idx == index)
                    return current.data;
                current = current.next;
                idx++;
            }
            return null;
        }

        public boolean isEmpty() {
            return size == 0;
        }

        public Iterator<T> iterator() {
            return new Iterator<T>() {
                Node current = head;
                Node lastReturned = null;
        
                public boolean hasNext() {
                    return current != null;
                }
        
                public T next() {
                    if (current == null) {
                        throw new NoSuchElementException();
                    }
                    lastReturned = current;
                    T data = current.data;
                    current = current.next;
                    return data;
                }
        
                public void remove() {
                    if (lastReturned == null) {
                        throw new IllegalStateException();
                    }
                    removeNode(lastReturned);
                    lastReturned = null;
                }
            };
        }        

        public int size() {
            return size;
        }
    }

    static class AVLTree<T> {
        class Node {
            T data;
            Node left, right;
            int height;

            Node(T data) {
                this.data = data;
                height = 1;
            }
        }

        Node root;
        Comparator<T> comparator;

        AVLTree(Comparator<T> comparator) {
            this.comparator = comparator;
        }

        int height(Node N) {
            return N == null ? 0 : N.height;
        }

        Node rightRotate(Node y) {
            Node x = y.left;
            Node T2 = x.right;

            x.right = y;
            y.left = T2;

            y.height = Math.max(height(y.left), height(y.right)) + 1;
            x.height = Math.max(height(x.left), height(x.right)) + 1;

            return x;
        }

        Node leftRotate(Node x) {
            Node y = x.right;
            Node T2 = y.left;

            y.left = x;
            x.right = T2;

            x.height = Math.max(height(x.left), height(x.right)) + 1;
            y.height = Math.max(height(y.left), height(y.right)) + 1;

            return y;
        }

        int getBalance(Node N) {
            return N == null ? 0 : height(N.left) - height(N.right);
        }

        void insert(T data) {
            root = insert(root, data);
        }

        Node insert(Node node, T data) {
            if (node == null)
                return new Node(data);

            int cmp = comparator.compare(data, node.data);
            if (cmp < 0)
                node.left = insert(node.left, data);
            else if (cmp > 0)
                node.right = insert(node.right, data);
            else
                return node; // Duplicate data not inserted

            node.height = 1 + Math.max(height(node.left), height(node.right));

            int balance = getBalance(node);

            // Left Left Case
            if (balance > 1 && comparator.compare(data, node.left.data) < 0)
                return rightRotate(node);

            // Right Right Case
            if (balance < -1 && comparator.compare(data, node.right.data) > 0)
                return leftRotate(node);

            // Left Right Case
            if (balance > 1 && comparator.compare(data, node.left.data) > 0) {
                node.left = leftRotate(node.left);
                return rightRotate(node);
            }

            // Right Left Case
            if (balance < -1 && comparator.compare(data, node.right.data) < 0) {
                node.right = rightRotate(node.right);
                return leftRotate(node);
            }

            return node;
        }

        void delete(T data) {
            root = delete(root, data);
        }

        Node delete(Node root, T data) {
            if (root == null)
                return root;

            int cmp = comparator.compare(data, root.data);
            if (cmp < 0)
                root.left = delete(root.left, data);
            else if (cmp > 0)
                root.right = delete(root.right, data);
            else {
                if ((root.left == null) || (root.right == null)) {
                    Node temp = null;
                    if (root.left != null)
                        temp = root.left;
                    else
                        temp = root.right;

                    if (temp == null) {
                        temp = root;
                        root = null;
                    } else
                        root = temp;
                } else {
                    Node temp = minValueNode(root.right);

                    root.data = temp.data;

                    root.right = delete(root.right, temp.data);
                }
            }

            if (root == null)
                return root;

            root.height = Math.max(height(root.left), height(root.right)) + 1;

            int balance = getBalance(root);

            // Left Left Case
            if (balance > 1 && getBalance(root.left) >= 0)
                return rightRotate(root);

            // Left Right Case
            if (balance > 1 && getBalance(root.left) < 0) {
                root.left = leftRotate(root.left);
                return rightRotate(root);
            }

            // Right Right Case
            if (balance < -1 && getBalance(root.right) <= 0)
                return leftRotate(root);

            // Right Left Case
            if (balance < -1 && getBalance(root.right) > 0) {
                root.right = rightRotate(root.right);
                return leftRotate(root);
            }

            return root;
        }

        Node minValueNode(Node node) {
            Node current = node;

            while (current.left != null)
                current = current.left;

            return current;
        }

        void inOrder(Node node, List<T> list) {
            if (node != null) {
                inOrder(node.left, list);
                list.add(node.data);
                inOrder(node.right, list);
            }
        }

        T first() {
            Node node = root;
            if (node == null)
                return null;
            while (node.left != null)
                node = node.left;
            return node.data;
        }

        T last() {
            Node node = root;
            if (node == null)
                return null;
            while (node.right != null)
                node = node.right;
            return node.data;
        }
    }

    public static void main(String[] args) throws IOException {
        InputStream inputStream = System.in;
        OutputStream outputStream = System.out;
        InputReader in = new InputReader(inputStream);
        out = new PrintWriter(outputStream);

        participants = new HashMap<>();
        teams = new HashMap<>();
        teamList = new LinkedList<>();
        eliminatedParticipants = new HashSet<>();

        // Comparator for Participants
        participantComparator = new Comparator<Participant>() {
            public int compare(Participant a, Participant b) {
                if (a.points != b.points) {
                    return b.points - a.points; // Descending points
                } else if (a.matches != b.matches) {
                    return a.matches - b.matches; // Ascending matches
                } else {
                    return a.id - b.id; // Ascending id
                }
            }
        };

        Comparator<Team> teamComparator = new Comparator<Team>() {
            public int compare(Team a, Team b) {
                if (a.totalPoints != b.totalPoints) {
                    return b.totalPoints - a.totalPoints; // Descending totalPoints
                } else if (a.memberCount != b.memberCount) {
                    return a.memberCount - b.memberCount; // Ascending memberCount
                } else {
                    return a.id - b.id; // Ascending id
                }
            }
        };
        teamTree = new AVLTree<>(teamComparator);

        int participantIdCounter = 1;
        int teamIdCounter = 1;

        int M = in.nextInteger();
        int[] Mi = new int[M];
        for (int i = 0; i < M; i++) {
            Mi[i] = in.nextInteger();
        }

        Team jokiTeam = null;
        Team sofitaTeam = null;

        for (int i = 0; i < M; i++) {
            int MiValue = Mi[i];
            Team team = new Team(teamIdCounter, participantComparator);

            for (int j = 0; j < MiValue; j++) {
                int points = in.nextInteger();
                Participant participant = new Participant(participantIdCounter++, points, team);
                participants.put(participant.id, participant);
                team.participants.insert(participant);
                team.totalPoints += points;
                team.memberCount++;

                if (team.pointCounts[points] == 0) {
                    team.uniquePointCount++;
                }
                team.pointCounts[points]++;
            }

            teams.put(team.id, team);
            teamList.add(team);
            teamTree.insert(team);

            if (team.id == 1) {
                sofitaTeam = team;
            }
            teamIdCounter++;
        }

        // Initialize Joki's team
        Team lowestTeam = null;
        Team secondLowestTeam = null;
        for (Team team : teamList) {
            if (lowestTeam == null || team.totalPoints < lowestTeam.totalPoints) {
                if (lowestTeam != null && lowestTeam != sofitaTeam) {
                    secondLowestTeam = lowestTeam;
                }
                lowestTeam = team;
            } else if ((secondLowestTeam == null || team.totalPoints < secondLowestTeam.totalPoints) && team != sofitaTeam) {
                secondLowestTeam = team;
            }
        }

        if (lowestTeam != sofitaTeam) {
            jokiTeam = lowestTeam;
        } else {
            jokiTeam = secondLowestTeam;
        }

        int Q = in.nextInteger();

        for (int q = 0; q < Q; q++) {
            String line = in.nextLine();
            if (line == null || line.trim().isEmpty()) {
                continue;
            }

            String[] parts = line.split(" ");
            if (parts.length == 0) {
                continue;
            }

            char command = parts[0].charAt(0);

            switch (command) {
                case 'A':
                    // Parse the number of new participants
                    long jumlahPesertaBaru = Long.parseLong(parts[1]);

                    for (int i = 0; i < jumlahPesertaBaru; i++) {
                        Participant participant = new Participant(participantIdCounter++, 3, sofitaTeam);
                        participants.put(participant.id, participant);
                        sofitaTeam.participants.insert(participant);
                        sofitaTeam.totalPoints += 3;
                        sofitaTeam.memberCount++;

                        if (sofitaTeam.pointCounts[3] == 0) {
                            sofitaTeam.uniquePointCount++;
                        }
                        sofitaTeam.pointCounts[3]++;
                    }

                    // Update teamTree
                    teamTree.delete(sofitaTeam);
                    teamTree.insert(sofitaTeam);

                    // Print the updated member count
                    out.println(sofitaTeam.memberCount);
                    break;
                case 'M':
                    String direction = parts[1];
                    int sofitaIndex = teamList.indexOf(sofitaTeam);
                    if (direction.equals("L")) {
                        sofitaIndex = (sofitaIndex - 1 + teamList.size()) % teamList.size();
                    } else if (direction.equals("R")) {
                        sofitaIndex = (sofitaIndex + 1) % teamList.size();
                    }
                    sofitaTeam = teamList.get(sofitaIndex);

                    // Check if Joki is in the same team
                    if (jokiTeam != null && sofitaTeam.id == jokiTeam.id) {
                        // Apply consequences based on timesCaught
                        sofitaTeam.timesCaught++;
                        if (sofitaTeam.timesCaught == 1) {
                            // First time caught: Remove top 3 participants
                            removeTopParticipants(sofitaTeam, participants, 3);
                        } else if (sofitaTeam.timesCaught == 2) {
                            // Second time caught: Set all participants' points to one
                            setAllParticipantsPointsToOne(sofitaTeam);
                        } else if (sofitaTeam.timesCaught >= 3) {
                            // Third time caught: Eliminate the team
                            eliminateTeam(sofitaTeam, teamList, teamTree, teams, participants);

                            // Move Sofita to team with highest total points
                            if (!teamList.isEmpty()) {
                                sofitaTeam = teamList.get(0);
                            } else {
                                sofitaTeam = null;
                            }
                        }
                        // Joki is expelled and moves to team with lowest total points
                        jokiTeam = getTeamWithLowestPointsExcept(teamList, sofitaTeam);
                    }

                    if (sofitaTeam != null && sofitaTeam.memberCount < 7) {
                        eliminateTeam(sofitaTeam, teamList, teamTree, teams, participants);
                        // Move Sofita to the team with highest total points
                        if (!teamList.isEmpty()) {
                            // Recalculate the team with highest total points
                            teamList = mergeSortTeams(teamList);
                            sofitaTeam = teamList.get(0);
                        } else {
                            sofitaTeam = null;
                        }
                    }

                    out.println(sofitaTeam != null ? sofitaTeam.id : -1);
                    break;
                case 'J':
                    String jokiDirection = parts[1];
                    int jokiIndex = teamList.indexOf(jokiTeam);
                    if (jokiDirection.equals("L")) {
                        jokiIndex = (jokiIndex - 1 + teamList.size()) % teamList.size();
                    } else if (jokiDirection.equals("R")) {
                        jokiIndex = (jokiIndex + 1) % teamList.size();
                    }
                    Team jokiNextTeam = teamList.get(jokiIndex);
                    if (jokiNextTeam.id != sofitaTeam.id) {
                        jokiTeam = jokiNextTeam;
                    }
                    out.println(jokiTeam.id);
                    break;
                case 'U':
                    out.println(sofitaTeam.uniquePointCount);
                    break;
                case 'G':
                    String directionG = parts[1];
                    Team newTeam = new Team(teamIdCounter++, participantComparator);
                    // Add seven new participants with points = 1
                    for (int i = 0; i < 7; i++) {
                        Participant participant = new Participant(participantIdCounter++, 1, newTeam);
                        participants.put(participant.id, participant);
                        newTeam.participants.insert(participant);
                        newTeam.totalPoints += 1;
                        newTeam.memberCount++;

                        if (newTeam.pointCounts[1] == 0) {
                            newTeam.uniquePointCount++;
                        }
                        newTeam.pointCounts[1]++;
                    }

                    teams.put(newTeam.id, newTeam);

                    // Insert the new team to the left or right of Sofita's current team
                    int sofitaIndexG = teamList.indexOf(sofitaTeam);
                    if (directionG.equals("L")) {
                        teamList.add(sofitaIndexG, newTeam);
                    } else if (directionG.equals("R")) {
                        teamList.add(sofitaIndexG + 1, newTeam);
                    }

                    // Insert into AVLTree
                    teamTree.insert(newTeam);

                    out.println(newTeam.id);
                    break;
                case 'T':
                    int senderId = Integer.parseInt(parts[1]);
                    int receiverId = Integer.parseInt(parts[2]);
                    int amount = Integer.parseInt(parts[3]);

                    Participant sender = participants.get(senderId);
                    Participant receiver = participants.get(receiverId);

                    if (sender == null || receiver == null || sender.team != sofitaTeam || receiver.team != sofitaTeam) {
                        out.println(-1);
                    } else if (amount >= sender.points) {
                        out.println(-1);
                    } else {
                        // Update sender's points
                        updateParticipantPoints(sender, -amount);

                        // Update receiver's points
                        updateParticipantPoints(receiver, amount);

                        out.println(sender.points + " " + receiver.points);
                    }
                    break;
                case 'V':
                    int participant1Id = Integer.parseInt(parts[1]);
                    int participant2Id = Integer.parseInt(parts[2]);
                    int opponentTeamId = Integer.parseInt(parts[3]);
                    int result = Integer.parseInt(parts[4]);

                    Participant participant1 = participants.get(participant1Id);
                    Participant participant2 = participants.get(participant2Id);
                    Team opponentTeam = teams.get(opponentTeamId);

                    if (participant1 == null || participant2 == null || participant1.team != sofitaTeam || participant2.team != opponentTeam) {
                        out.println(-1);
                    } else {
                        participant1.matches++;
                        participant2.matches++;

                        // Remove and re-insert participants to update their position in the AVL Tree
                        participant1.team.participants.delete(participant1);
                        participant2.team.participants.delete(participant2);

                        if (result == 0) {
                            // Draw
                            updateParticipantPoints(participant1, 1);
                            updateParticipantPoints(participant2, 1);
                            out.println(participant1.points + " " + participant2.points);
                        } else if (result == 1) {
                            // Participant1 wins
                            updateParticipantPoints(participant1, 3);
                            updateParticipantPoints(participant2, -3);
                            out.println(participant1.points);
                        } else if (result == -1) {
                            // Participant2 wins
                            updateParticipantPoints(participant1, -3);
                            updateParticipantPoints(participant2, 3);
                            out.println(participant2.points);
                        }

                        // Re-insert participants
                        participant1.team.participants.insert(participant1);
                        participant2.team.participants.insert(participant2);

                        // Update teams in teamTree
                        teamTree.delete(participant1.team);
                        teamTree.insert(participant1.team);

                        teamTree.delete(participant2.team);
                        teamTree.insert(participant2.team);
                    }
                    break;
                case 'E':
                    int threshold = Integer.parseInt(parts[1]);

                    List<Team> eliminatedTeams = new ArrayList<>();
                    boolean sofitaTeamEliminated = false;

                    Iterator<Team> iterator = teamList.iterator();
                    while (iterator.hasNext()) {
                        Team team = iterator.next();
                        if (team.totalPoints < threshold) {
                            eliminatedTeams.add(team);

                            if (team == sofitaTeam) {
                                sofitaTeamEliminated = true;
                            }

                            eliminateTeam(team, teamList, teamTree, teams, participants);
                            iterator.remove();
                        }
                    }

                    // If Sofita's team was eliminated
                    if (sofitaTeamEliminated) {
                        if (!teamList.isEmpty()) {
                            // Recalculate the team with highest total points
                            teamList = mergeSortTeams(teamList);
                            sofitaTeam = teamList.get(0);
                        } else {
                            sofitaTeam = null;
                        }
                    }

                    out.println(eliminatedTeams.size());
                    break;
                case 'R':
                    // Rearrange teams by sorting the teamList
                    teamList = mergeSortTeams(teamList);

                    if (!teamList.isEmpty()) {
                        sofitaTeam = teamList.get(0);

                        // Check if Joki is in the same team
                        if (jokiTeam != null && sofitaTeam.id == jokiTeam.id) {
                            // Apply consequences based on timesCaught
                            sofitaTeam.timesCaught++;
                            if (sofitaTeam.timesCaught == 1) {
                                // First time caught: Remove top 3 participants
                                removeTopParticipants(sofitaTeam, participants, 3);
                            } else if (sofitaTeam.timesCaught == 2) {
                                // Second time caught: Set all participants' points to one
                                setAllParticipantsPointsToOne(sofitaTeam);
                            } else if (sofitaTeam.timesCaught >= 3) {
                                // Third time caught: Eliminate the team
                                eliminateTeam(sofitaTeam, teamList, teamTree, teams, participants);

                                // Move Sofita to team with highest total points
                                if (!teamList.isEmpty()) {
                                    sofitaTeam = teamList.get(0);
                                } else {
                                    sofitaTeam = null;
                                }
                            }
                            // Joki is expelled and moves to team with lowest total points
                            jokiTeam = getTeamWithLowestPointsExcept(teamList, sofitaTeam);
                        }

                        out.println(sofitaTeam != null ? sofitaTeam.id : -1);
                    } else {
                        out.println(-1);
                    }
                    break;
                case 'B':
                    String boundType = parts[1];

                    int K = sofitaTeam.memberCount;
                    List<Participant> participantList = new ArrayList<>();
                    sofitaTeam.participants.inOrder(sofitaTeam.participants.root, participantList);

                    int[] pointsArray = new int[K];
                    int idx = 0;
                    for (Participant p : participantList) {
                        pointsArray[idx++] = p.points;
                    }

                    // Since participants are already sorted, pointsArray is sorted
                    // Now compute Q1, Q3, IQR, L, U
                    int Q1Index = (int) Math.floor(0.25 * (K + 1)) - 1;
                    int Q3Index = (int) Math.floor(0.75 * (K + 1)) - 1;

                    Q1Index = Math.max(0, Math.min(Q1Index, K - 1));
                    Q3Index = Math.max(0, Math.min(Q3Index, K - 1));

                    int Q1 = pointsArray[Q1Index];
                    int Q3 = pointsArray[Q3Index];
                    int IQR = Q3 - Q1;

                    int L = Q1 - (int) (1.5 * IQR);
                    int U = Q3 + (int) (1.5 * IQR);

                    int count = 0;
                    if (boundType.equals("U")) {
                        for (int point : pointsArray) {
                            if (point > U) {
                                count++;
                            }
                        }
                    } else if (boundType.equals("L")) {
                        for (int point : pointsArray) {
                            if (point < L) {
                                count++;
                            }
                        }
                    }
                    out.println(count);
                    break;
                default:
                    out.println("Unknown command: " + command);
                    break;
            }
        }

        out.close();
    }

    private static void updateParticipantPoints(Participant participant, int delta) {
        Team team = participant.team;
        int oldPoints = participant.points;
        int newPoints = participant.points + delta;

        // Update team point counts
        team.pointCounts[oldPoints]--;
        if (team.pointCounts[oldPoints] == 0) {
            team.uniquePointCount--;
        }

        participant.points = newPoints;
        team.totalPoints += delta;

        if (newPoints > 0) {
            if (team.pointCounts[newPoints] == 0) {
                team.uniquePointCount++;
            }
            team.pointCounts[newPoints]++;
        } else {
            // Remove participant if points reach zero
            team.participants.delete(participant);
            team.memberCount--;
        }

        // Update team in teamTree
        teamTree.delete(team);
        teamTree.insert(team);
    }


    private static void removeTopParticipants(Team team, Map<Integer, Participant> participantsMap, int count) {
        // Get participants in order
        List<Participant> participantList = new ArrayList<>();
        team.participants.inOrder(team.participants.root, participantList);

        int removed = 0;
        for (Participant p : participantList) {
            if (removed >= count) break;
            removeParticipantFromTeam(p, participantsMap);
            removed++;
        }
    }

    private static void setAllParticipantsPointsToOne(Team team) {
        // Reset team point counts
        team.pointCounts = new int[1001];
        team.uniquePointCount = 1; // Since all points will be 1
        team.totalPoints = team.memberCount * 1;

        // Collect participants
        List<Participant> participantList = new ArrayList<>();
        team.participants.inOrder(team.participants.root, participantList);

        // Clear participants tree
        team.participants = new AVLTree<>(team.participants.comparator);

        for (Participant p : participantList) {
            p.points = 1;
            team.participants.insert(p);
        }
        team.pointCounts[1] = team.memberCount;

        // Update team in teamTree
        teamTree.delete(team);
        teamTree.insert(team);
    }

    private static void eliminateTeam(Team team, LinkedList<Team> teamList, AVLTree<Team> teamTree, Map<Integer, Team> teamsMap, Map<Integer, Participant> participantsMap) {
        teamList.remove(team);
        teamTree.delete(team);
        teamsMap.remove(team.id);
        // Remove all participants from the participants map
        List<Participant> participantList = new ArrayList<>();
        team.participants.inOrder(team.participants.root, participantList);
        for (Participant p : participantList) {
            participantsMap.remove(p.id);
        }
    }

    private static Team getTeamWithLowestPointsExcept(LinkedList<Team> teamList, Team excludeTeam) {
        Team lowestTeam = null;
        for (Team team : teamList) {
            if (team == excludeTeam) continue;
            if (lowestTeam == null || team.totalPoints < lowestTeam.totalPoints) {
                lowestTeam = team;
            }
        }
        return lowestTeam;
    }

    public static LinkedList<Team> mergeSortTeams(LinkedList<Team> list) {
        if (list.size() <= 1) {
            return list;
        }
        LinkedList<Team> left = new LinkedList<>();
        LinkedList<Team> right = new LinkedList<>();
        int index = 0;
        int middle = list.size() / 2;
        for (Team team : list) {
            if (index < middle) {
                left.add(team);
            } else {
                right.add(team);
            }
            index++;
        }
        left = mergeSortTeams(left);
        right = mergeSortTeams(right);
        return mergeTeams(left, right);
    }

    public static LinkedList<Team> mergeTeams(LinkedList<Team> left, LinkedList<Team> right) {
        LinkedList<Team> result = new LinkedList<>();
        Iterator<Team> itLeft = left.iterator();
        Iterator<Team> itRight = right.iterator();
        Team teamLeft = itLeft.hasNext() ? itLeft.next() : null;
        Team teamRight = itRight.hasNext() ? itRight.next() : null;
        while (teamLeft != null || teamRight != null) {
            if (teamLeft == null) {
                result.add(teamRight);
                teamRight = itRight.hasNext() ? itRight.next() : null;
            } else if (teamRight == null) {
                result.add(teamLeft);
                teamLeft = itLeft.hasNext() ? itLeft.next() : null;
            } else {
                int cmp = teamLeft.compareTo(teamRight);
                if (cmp <= 0) {
                    result.add(teamLeft);
                    teamLeft = itLeft.hasNext() ? itLeft.next() : null;
                } else {
                    result.add(teamRight);
                    teamRight = itRight.hasNext() ? itRight.next() : null;
                }
            }
        }
        return result;
    }

    private static void removeParticipantFromTeam(Participant participant, Map<Integer, Participant> participantsMap) {
        Team team = participant.team;
        int points = participant.points;

        team.participants.delete(participant);
        team.memberCount--;
        team.totalPoints -= points;
        team.pointCounts[points]--;
        if (team.pointCounts[points] == 0) {
            team.uniquePointCount--;
        }
        participantsMap.remove(participant.id);

        // Update team in teamTree
        teamTree.delete(team);
        teamTree.insert(team);
    }

    static class InputReader {
        public BufferedReader reader;
        public StringTokenizer tokenizer;

        public InputReader(InputStream stream) {
            reader = new BufferedReader(new InputStreamReader(stream), 32768);
            tokenizer = null;
        }

        public String nextLine() {
            tokenizer = null;
            try {
                return reader.readLine();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
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
            String next = next();
            if (next == null) {
                return -1;
            }
            return Integer.parseInt(next);
        }
    }
}
