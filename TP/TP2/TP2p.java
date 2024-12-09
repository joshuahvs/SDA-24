import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.StringTokenizer;

public class TP2p {
    private static PrintWriter out;

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

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null || getClass() != obj.getClass())
                return false;
            Participant other = (Participant) obj;
            return this.id == other.id;
        }

        @Override
        public int hashCode() {
            return Integer.hashCode(id);
        }
    }

    static class Team {
        int id;
        MyLinkedList<Participant> participants;
        int totalPoints;
        int memberCount;
        int uniquePointCount;
        PointCountAVLTree pointCounts;
        int timesCaught;
        MyTreeMap<ParticipantKey, Participant> participantTree;

        public Team(int id) {
            this.id = id;
            this.participants = new MyLinkedList<>();
            this.totalPoints = 0;
            this.memberCount = 0;
            this.uniquePointCount = 0;
            this.pointCounts = new PointCountAVLTree();
            this.timesCaught = 0;
            this.participantTree = new MyTreeMap<>();
        }
    }

    static MyTreeMap<Integer, Participant> participants = new MyTreeMap<>();
    static MyTreeMap<Integer, Team> teams = new MyTreeMap<>();
    static MyLinkedList<Team> teamList = new MyLinkedList<>();

    static int participantIdCounter = 1;
    static int teamIdCounter = 1;

    public static void main(String[] args) throws IOException {
        InputStream inputStream = System.in;
        OutputStream outputStream = System.out;
        InputReader in = new InputReader(inputStream);
        out = new PrintWriter(outputStream);

        int M = in.nextInt();
        int[] Mi = new int[M];
        for (int i = 0; i < M; i++) {
            Mi[i] = in.nextInt();
        }

        Team jokiTeam = null;
        Team sofitaTeam = null;

        for (int i = 0; i < M; i++) {
            int MiValue = Mi[i];
            Team team = new Team(teamIdCounter);

            for (int j = 0; j < MiValue; j++) {
                int points = in.nextInt();
                Participant participant = new Participant(participantIdCounter++, points, team);
                participants.put(participant.id, participant);
                team.participants.add(participant);
                team.totalPoints += points;
                team.memberCount++;

                // Update pointCounts
                long currentCount = team.pointCounts.get(participant.points);
                if (currentCount == 0) {
                    team.uniquePointCount++;
                }
                team.pointCounts.put(participant.points, currentCount + 1);

                // Add participant to participantTree
                team.participantTree.put(new ParticipantKey(participant), participant);
            }

            teams.put(team.id, team);
            teamList.add(team);
            if (team.id == 1) {
                sofitaTeam = team;
            }
            teamIdCounter++;
        }

        // Find the team with the lowest points (jokiTeam)
        jokiTeam = getTeamWithLowestPointsExcept(teamList, sofitaTeam);

        int Q = in.nextInt();

        for (int q = 0; q < Q; q++) {
            String line = in.nextLine();
            if (line == null || line.trim().isEmpty()) {
                q--;
                continue;
            }

            String[] parts = line.split(" ");
            if (parts.length == 0) {
                continue;
            }

            char command = parts[0].charAt(0);

            if (command == 'A') {
                // Parse the number of new participants
                int jumlahPesertaBaru = Integer.parseInt(parts[1]);

                for (int i = 0; i < jumlahPesertaBaru; i++) {
                    Participant participant = new Participant(participantIdCounter++, 3, sofitaTeam);
                    participants.put(participant.id, participant);
                    sofitaTeam.participants.add(participant);
                    sofitaTeam.totalPoints += 3;
                    sofitaTeam.memberCount++;

                    long currentCount = sofitaTeam.pointCounts.get(3);
                    if (currentCount == 0) {
                        sofitaTeam.uniquePointCount++;
                    }
                    sofitaTeam.pointCounts.put(3, currentCount + 1);

                    // Add participant to participantTree
                    sofitaTeam.participantTree.put(new ParticipantKey(participant), participant);
                }

                // Print the updated member count
                out.println(sofitaTeam.memberCount);
            } else if (command == 'B') {
                String boundType = parts[1];
                int K = sofitaTeam.memberCount;
                int indexQ1 = Math.max(0, (int) Math.floor(0.25 * (K - 1)));
                int indexQ3 = Math.min(K - 1, (int) Math.floor(0.75 * (K - 1)));

                Integer Q1 = sofitaTeam.pointCounts.findKthSmallest(indexQ1 + 1);
                Integer Q3 = sofitaTeam.pointCounts.findKthSmallest(indexQ3 + 1);
                int IQR = Q3 - Q1;

                int L = Q1 - (int) (1.5 * IQR);
                int U = Q3 + (int) (1.5 * IQR);

                long count = 0;
                if (boundType.equals("U")) {
                    count = sofitaTeam.pointCounts.countGreaterThan(U);
                } else if (boundType.equals("L")) {
                    count = sofitaTeam.pointCounts.countLessThan(L);
                }
                out.println(count);
            } else if (command == 'M') {
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
                        eliminateTeam(sofitaTeam, teamList, teams, participants);
                        // Move Sofita to team with highest total points
                        if (!teamList.isEmpty()) {
                            sofitaTeam = getTeamWithHighestPoints(teamList);
                        } else {
                            sofitaTeam = null;
                        }
                    }
                    // Joki is expelled and moves to team with lowest total points
                    jokiTeam = getTeamWithLowestPointsExcept(teamList, sofitaTeam);
                }

                if (sofitaTeam != null && sofitaTeam.memberCount < 7) {
                    eliminateTeam(sofitaTeam, teamList, teams, participants);
                    // Move Sofita to the team with highest total points
                    if (!teamList.isEmpty()) {
                        sofitaTeam = getTeamWithHighestPoints(teamList);
                    } else {
                        sofitaTeam = null;
                    }
                }
                if (sofitaTeam != null) {
                    out.println(sofitaTeam.id);
                } else {
                    out.println(-1);
                }
            } else if (command == 'T') {
                int senderId = Integer.parseInt(parts[1]);
                int receiverId = Integer.parseInt(parts[2]);
                int amount = Integer.parseInt(parts[3]);

                Participant sender = getParticipantById(senderId);
                Participant receiver = getParticipantById(receiverId);

                if (sender == null || receiver == null || sender.team != sofitaTeam
                        || receiver.team != sofitaTeam) {
                    out.println(-1);
                } else if (amount >= sender.points) {
                    out.println(-1);
                } else {
                    // Update sender's points
                    updateParticipantPoints(sender, -amount);
                    // Update receiver's points
                    updateParticipantPoints(receiver, amount);

                    // Check if sender's team needs to be eliminated
                    if (sender.team.memberCount < 7) {
                        eliminateTeam(sender.team, teamList, teams, participants);

                        // Move Sofita to the team with highest total points
                        if (!teamList.isEmpty()) {
                            sofitaTeam = getTeamWithHighestPoints(teamList);
                        } else {
                            sofitaTeam = null;
                        }
                    }
                    out.println(sender.points + " " + receiver.points);
                }
            } else if (command == 'G') {
                String directionG = parts[1];
                Team newTeam = new Team(teamIdCounter);

                for (int i = 0; i < 7; i++) {
                    int points = 1; // Participants have points = 1
                    Participant participant = new Participant(participantIdCounter++, points, newTeam);
                    participants.put(participant.id, participant);
                    newTeam.participants.add(participant);
                    newTeam.totalPoints += points; // Correctly add 1 to totalPoints
                    newTeam.memberCount++;

                    // Update uniquePointCount and pointCounts correctly
                    long currentCount = newTeam.pointCounts.get(points);
                    if (currentCount == 0) {
                        newTeam.uniquePointCount++;
                    }
                    newTeam.pointCounts.put(points, currentCount + 1);

                    // Add participant to participantTree
                    newTeam.participantTree.put(new ParticipantKey(participant), participant);
                }

                int sofitaIndex = teamList.indexOf(sofitaTeam);
                if (directionG.equals("L")) {
                    teamList.add(sofitaIndex, newTeam);
                } else if (directionG.equals("R")) {
                    teamList.add(sofitaIndex + 1, newTeam);
                }

                teams.put(newTeam.id, newTeam);
                teamIdCounter++;
                out.println(newTeam.id);
            } else if (command == 'V') {
                int participant1Id = Integer.parseInt(parts[1]);
                int participant2Id = Integer.parseInt(parts[2]);
                int opponentTeamId = Integer.parseInt(parts[3]);
                int result = Integer.parseInt(parts[4]);

                Participant participant1 = getParticipantById(participant1Id);
                Team opponentTeam = teams.get(opponentTeamId);
                Participant participant2 = getParticipantById(participant2Id);

                if (participant1 == null || participant2 == null || participant1.team != sofitaTeam
                        || participant2.team != opponentTeam) {
                    out.println(-1);
                } else {
                    participant1.matches++;
                    participant2.matches++;

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
                }
            } else if (command == 'E') {
                int threshold = Integer.parseInt(parts[1]);
                MyLinkedList<Team> eliminatedTeams = new MyLinkedList<>();
                boolean sofitaTeamEliminated = false;

                // Collect teams to be eliminated
                for (int i = 0; i < teamList.size(); i++) {
                    Team team = teamList.get(i);
                    if (team.totalPoints < threshold) {
                        eliminatedTeams.add(team);
                    }
                }

                // Eliminate the collected teams
                for (int i = 0; i < eliminatedTeams.size(); i++) {
                    Team team = eliminatedTeams.get(i);
                    eliminateTeam(team, teamList, teams, participants);

                    if (team == sofitaTeam) {
                        sofitaTeamEliminated = true;
                    }
                    if (team == jokiTeam) {
                        jokiTeam = getTeamWithLowestPointsExcept(teamList, sofitaTeam);
                    }
                }

                // If Sofita's team was eliminated
                if (sofitaTeamEliminated) {
                    if (!teamList.isEmpty()) {
                        // Move Sofita to the team with the highest total points
                        sofitaTeam = getTeamWithHighestPoints(teamList);
                    } else {
                        sofitaTeam = null;
                    }
                }
                out.println(eliminatedTeams.size());
            } else if (command == 'U') {
                out.println(sofitaTeam.uniquePointCount);
            } else if (command == 'R') {
                mergeSortTeams(teamList, 0, teamList.size() - 1);
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
                            eliminateTeam(sofitaTeam, teamList, teams, participants);
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
            } else if (command == 'J') {
                String jokiDirection = parts[1];
                int jokiIndex = teamList.indexOf(jokiTeam);
                if (jokiDirection.equals("L")) {
                    jokiIndex = (jokiIndex - 1 + teamList.size()) % teamList.size();
                } else if (jokiDirection.equals("R")) {
                    jokiIndex = (jokiIndex + 1) % teamList.size();
                }
                Team destinationTeam = teamList.get(jokiIndex);
                if (destinationTeam.id != sofitaTeam.id) {
                    jokiTeam = teamList.get(jokiIndex);
                }
                out.println(jokiTeam.id);
            }
        }
        out.close();
    }

    private static Team getTeamWithLowestPointsExcept(MyLinkedList<Team> teamList, Team excludeTeam) {
        Team lowestTeam = null;
        for (int i = 0; i < teamList.size(); i++) {
            Team team = teamList.get(i);
            if (team == excludeTeam)
                continue;
            if (lowestTeam == null || team.totalPoints < lowestTeam.totalPoints) {
                lowestTeam = team;
            }
        }
        return lowestTeam;
    }

    private static void removeTopParticipants(Team team, MyTreeMap<Integer, Participant> participants, int numParticipants) {
        // Get the top participants based on the criteria
        java.util.List<MyTreeMap.TreeNode<ParticipantKey, Participant>> topNodes = team.participantTree.getTopK(numParticipants);
        for (MyTreeMap.TreeNode<ParticipantKey, Participant> node : topNodes) {
            Participant p = node.value;

            // Remove participant from participantTree
            team.participantTree.remove(new ParticipantKey(p));

            // Remove participant from team.participants (the linked list)
            team.participants.remove(p);

            // Update team stats
            team.totalPoints -= p.points;
            team.memberCount--;

            // Update pointCounts and uniquePointCount
            long count = team.pointCounts.get(p.points);
            if (count == 1) {
                team.pointCounts.remove(p.points);
                team.uniquePointCount--;
            } else {
                team.pointCounts.put(p.points, count - 1);
            }

            // Remove participant from the global participants map
            participants.remove(p.id);
        }
    }

    private static void setAllParticipantsPointsToOne(Team team) {
        team.totalPoints = team.memberCount; // Since all points will be 1
        team.pointCounts = new PointCountAVLTree();
        team.uniquePointCount = 1; // Only one unique point value now
        team.pointCounts.put(1, (long) team.memberCount);

        // Clear participantTree and rebuild it
        team.participantTree = new MyTreeMap<>();

        MyLinkedList.Node<Participant> currentNode = team.participants.head;
        while (currentNode != null) {
            Participant p = currentNode.data;
            p.points = 1;
            // Create new ParticipantKey
            ParticipantKey key = new ParticipantKey(p);
            // Insert into participantTree
            team.participantTree.put(key, p);

            currentNode = currentNode.next;
        }
    }

    private static void eliminateTeam(Team team, MyLinkedList<Team> teamList, MyTreeMap<Integer, Team> teams,
            MyTreeMap<Integer, Participant> participants) {
        // Remove team from teamList
        teamList.remove(team);

        // Remove team from teams map
        teams.remove(team.id);

        // Collect participants to remove
        MyLinkedList.Node<Participant> currentNode = team.participants.head;
        while (currentNode != null) {
            Participant p = currentNode.data;
            // Remove participant from participants map
            participants.remove(p.id);
            currentNode = currentNode.next;
        }

        // Clear team's participant list and reset statistics
        team.participants = new MyLinkedList<>();
        team.memberCount = 0;
        team.totalPoints = 0;
        team.pointCounts = new PointCountAVLTree();
        team.uniquePointCount = 0;
        team.participantTree = new MyTreeMap<>();
    }

    private static Team getTeamWithHighestPoints(MyLinkedList<Team> teamList) {
        Team highestTeam = null;
        for (int i = 0; i < teamList.size(); i++) {
            Team team = teamList.get(i);
            if (highestTeam == null || team.totalPoints > highestTeam.totalPoints) {
                highestTeam = team;
            }
        }
        return highestTeam;
    }

    private static Participant getParticipantById(int id) {
        return participants.get(id);
    }

    private static void eliminateParticipant(Participant participant, Team team) {
        // Remove participant from participants map
        participants.remove(participant.id);

        // Remove participant from team's participant list
        team.participants.remove(participant);

        // Update team member count
        team.memberCount--;

        // Remove participant from participantTree
        ParticipantKey key = new ParticipantKey(participant);
        team.participantTree.remove(key);

        // Update pointCounts and uniquePointCount
        long count = team.pointCounts.get(participant.points);
        if (count == 1) {
            team.pointCounts.remove(participant.points);
            team.uniquePointCount--;
        } else {
            team.pointCounts.put(participant.points, count - 1);
        }
    }

    private static void updateParticipantPoints(Participant participant, int pointChange) {
        Team team = participant.team;

        // Remove old key from participantTree
        ParticipantKey oldKey = new ParticipantKey(participant);
        team.participantTree.remove(oldKey);

        // Update pointCounts and uniquePointCount for old points
        long oldCount = team.pointCounts.get(participant.points);
        if (oldCount == 1) {
            team.pointCounts.remove(participant.points);
            team.uniquePointCount--;
        } else {
            team.pointCounts.put(participant.points, oldCount - 1);
        }

        // Update participant's points
        participant.points += pointChange;

        // Update team's totalPoints
        team.totalPoints += pointChange;

        if (participant.points > 0) {
            // Update pointCounts and uniquePointCount for new points
            long newCount = team.pointCounts.get(participant.points);
            if (newCount == 0) {
                team.uniquePointCount++;
            }
            team.pointCounts.put(participant.points, newCount + 1);

            // Re-insert participant into participantTree with updated points
            ParticipantKey newKey = new ParticipantKey(participant);
            team.participantTree.put(newKey, participant);
        } else {
            // Participant's points have dropped to zero or negative; eliminate them
            eliminateParticipant(participant, team);
        }
    }

    public static void mergeSortTeams(MyLinkedList<Team> list, int left, int right) {
        if (left < right) {
            int mid = (left + right) / 2;
            mergeSortTeams(list, left, mid);
            mergeSortTeams(list, mid + 1, right);
            mergeTeams(list, left, mid, right);
        }
    }

    public static void mergeTeams(MyLinkedList<Team> list, int left, int mid, int right) {
        int n1 = mid - left + 1;
        int n2 = right - mid;

        Team[] leftArray = new Team[n1];
        Team[] rightArray = new Team[n2];

        for (int i = 0; i < n1; ++i)
            leftArray[i] = list.get(left + i);
        for (int j = 0; j < n2; ++j)
            rightArray[j] = list.get(mid + 1 + j);

        int i = 0, j = 0, k = left;
        while (i < n1 && j < n2) {
            Team a = leftArray[i];
            Team b = rightArray[j];
            int cmp = compareTeams(a, b);
            if (cmp <= 0) {
                list.set(k, leftArray[i]);
                i++;
            } else {
                list.set(k, rightArray[j]);
                j++;
            }
            k++;
        }
        while (i < n1) {
            list.set(k, leftArray[i]);
            i++;
            k++;
        }
        while (j < n2) {
            list.set(k, rightArray[j]);
            j++;
            k++;
        }
    }

    public static int compareTeams(Team a, Team b) {
        if (b.totalPoints != a.totalPoints) {
            return b.totalPoints - a.totalPoints;
        } else if (a.memberCount != b.memberCount) {
            return a.memberCount - b.memberCount;
        } else {
            return a.id - b.id;
        }
    }

    static class ParticipantKey implements Comparable<ParticipantKey> {
        Participant participant;

        public ParticipantKey(Participant participant) {
            this.participant = participant;
        }

        @Override
        public int compareTo(ParticipantKey other) {
            // First by points (descending)
            if (this.participant.points != other.participant.points) {
                return Integer.compare(other.participant.points, this.participant.points);
            }
            // Then by matches (ascending)
            if (this.participant.matches != other.participant.matches) {
                return Integer.compare(this.participant.matches, other.participant.matches);
            }
            // Then by ID (ascending)
            return Integer.compare(this.participant.id, other.participant.id);
        }
    }

    // Custom LinkedList implementation
    static class MyLinkedList<E> {
        Node<E> head;
        Node<E> tail;
        private int size;

        static class Node<E> {
            E data;
            Node<E> next;
            Node<E> prev;

            public Node(E data) {
                this.data = data;
            }
        }

        public void add(E element) {
            Node<E> newNode = new Node<>(element);
            if (head == null) {
                head = tail = newNode;
            } else {
                tail.next = newNode;
                newNode.prev = tail;
                tail = newNode;
            }
            size++;
        }

        public void add(int index, E element) {
            if (index < 0 || index > size)
                throw new IndexOutOfBoundsException();
            Node<E> newNode = new Node<>(element);
            if (index == size) {
                add(element);
                return;
            }
            Node<E> current = getNode(index);
            if (current == head) {
                newNode.next = head;
                head.prev = newNode;
                head = newNode;
            } else {
                Node<E> prevNode = current.prev;
                prevNode.next = newNode;
                newNode.prev = prevNode;
                newNode.next = current;
                current.prev = newNode;
            }
            size++;
        }

        public E get(int index) {
            if (index < 0 || index >= size)
                throw new IndexOutOfBoundsException();
            Node<E> current = getNode(index);
            return current.data;
        }

        public void set(int index, E element) {
            if (index < 0 || index >= size)
                throw new IndexOutOfBoundsException();
            Node<E> current = getNode(index);
            current.data = element;
        }

        private Node<E> getNode(int index) {
            Node<E> current;
            if (index < size / 2) {
                current = head;
                for (int i = 0; i < index; i++)
                    current = current.next;
            } else {
                current = tail;
                for (int i = size - 1; i > index; i--)
                    current = current.prev;
            }
            return current;
        }

        public boolean remove(E element) {
            Node<E> current = head;
            while (current != null) {
                if (current.data.equals(element)) {
                    if (current == head) {
                        head = head.next;
                        if (head != null)
                            head.prev = null;
                    } else if (current == tail) {
                        tail = tail.prev;
                        if (tail != null)
                            tail.next = null;
                    } else {
                        current.prev.next = current.next;
                        current.next.prev = current.prev;
                    }
                    size--;
                    return true;
                }
                current = current.next;
            }
            return false;
        }

        public int size() {
            return size;
        }

        public int indexOf(E element) {
            Node<E> current = head;
            int index = 0;
            while (current != null) {
                if (current.data.equals(element))
                    return index;
                current = current.next;
                index++;
            }
            return -1;
        }

        public boolean isEmpty() {
            return size == 0;
        }
    }

    // Custom TreeMap implementation using AVL Tree
    static class MyTreeMap<K extends Comparable<K>, V> {
        private TreeNode<K, V> root;

        static class TreeNode<K, V> {
            K key;
            V value;
            TreeNode<K, V> left;
            TreeNode<K, V> right;
            int height;

            public TreeNode(K key, V value) {
                this.key = key;
                this.value = value;
                this.height = 1;
            }
        }

        public void put(K key, V value) {
            root = insert(root, key, value);
        }

        public V get(K key) {
            TreeNode<K, V> node = getNode(root, key);
            return node == null ? null : node.value;
        }

        public boolean containsKey(K key) {
            return getNode(root, key) != null;
        }

        public void remove(K key) {
            root = deleteNode(root, key);
        }

        private TreeNode<K, V> getNode(TreeNode<K, V> node, K key) {
            if (node == null)
                return null;
            int cmp = key.compareTo(node.key);
            if (cmp < 0)
                return getNode(node.left, key);
            else if (cmp > 0)
                return getNode(node.right, key);
            else
                return node;
        }

        private TreeNode<K, V> insert(TreeNode<K, V> node, K key, V value) {
            if (node == null)
                return new TreeNode<>(key, value);
            int cmp = key.compareTo(node.key);
            if (cmp < 0)
                node.left = insert(node.left, key, value);
            else if (cmp > 0)
                node.right = insert(node.right, key, value);
            else
                node.value = value;
            node.height = 1 + Math.max(height(node.left), height(node.right));
            return balance(node);
        }

        private TreeNode<K, V> deleteNode(TreeNode<K, V> root, K key) {
            if (root == null)
                return root;
            int cmp = key.compareTo(root.key);
            if (cmp < 0)
                root.left = deleteNode(root.left, key);
            else if (cmp > 0)
                root.right = deleteNode(root.right, key);
            else {
                if ((root.left == null) || (root.right == null)) {
                    TreeNode<K, V> temp = (root.left != null) ? root.left : root.right;
                    if (temp == null) {
                        temp = root;
                        root = null;
                    } else
                        root = temp;
                } else {
                    TreeNode<K, V> temp = minValueNode(root.right);
                    root.key = temp.key;
                    root.value = temp.value;
                    root.right = deleteNode(root.right, temp.key);
                }
            }
            if (root == null)
                return root;
            root.height = 1 + Math.max(height(root.left), height(root.right));
            return balance(root);
        }

        private TreeNode<K, V> minValueNode(TreeNode<K, V> node) {
            TreeNode<K, V> current = node;
            while (current.left != null)
                current = current.left;
            return current;
        }

        private int height(TreeNode<K, V> node) {
            return node == null ? 0 : node.height;
        }

        private TreeNode<K, V> balance(TreeNode<K, V> y) {
            int balanceFactor = getBalance(y);
            if (balanceFactor > 1) {
                if (getBalance(y.left) >= 0) {
                    return rightRotate(y);
                } else {
                    y.left = leftRotate(y.left);
                    return rightRotate(y);
                }
            }
            if (balanceFactor < -1) {
                if (getBalance(y.right) <= 0) {
                    return leftRotate(y);
                } else {
                    y.right = rightRotate(y.right);
                    return leftRotate(y);
                }
            }
            return y;
        }

        private int getBalance(TreeNode<K, V> node) {
            return node == null ? 0 : height(node.left) - height(node.right);
        }

        private TreeNode<K, V> rightRotate(TreeNode<K, V> y) {
            TreeNode<K, V> x = y.left;
            TreeNode<K, V> T2 = x.right;

            x.right = y;
            y.left = T2;

            y.height = Math.max(height(y.left), height(y.right)) + 1;
            x.height = Math.max(height(x.left), height(x.right)) + 1;

            return x;
        }

        private TreeNode<K, V> leftRotate(TreeNode<K, V> x) {
            TreeNode<K, V> y = x.right;
            TreeNode<K, V> T2 = y.left;

            y.left = x;
            x.right = T2;

            x.height = Math.max(height(x.left), height(x.right)) + 1;
            y.height = Math.max(height(y.left), height(y.right)) + 1;

            return y;
        }

        public java.util.List<TreeNode<K, V>> getTopK(int k) {
            java.util.List<TreeNode<K, V>> result = new java.util.ArrayList<>();
            getTopK(root, result, k);
            return result;
        }

        private void getTopK(TreeNode<K, V> node, java.util.List<TreeNode<K, V>> result, int k) {
            if (node == null || result.size() >= k)
                return;
            getTopK(node.left, result, k); // Visit left subtree first
            if (result.size() < k)
                result.add(node);
            getTopK(node.right, result, k); // Then right subtree
        }
    }

    // Specialized AVL tree for point counts
    static class PointCountAVLTree {
        private TreeNode root;

        static class TreeNode {
            int pointValue;
            long count;
            TreeNode left;
            TreeNode right;
            int height;
            long size; // Total count of participants in subtree

            public TreeNode(int pointValue, long count) {
                this.pointValue = pointValue;
                this.count = count;
                this.height = 1;
                this.size = count;
            }
        }

        public void put(int pointValue, long count) {
            root = insert(root, pointValue, count);
        }

        public long get(int pointValue) {
            TreeNode node = getNode(root, pointValue);
            return node == null ? 0 : node.count;
        }

        public void remove(int pointValue) {
            root = deleteNode(root, pointValue);
        }

        private TreeNode getNode(TreeNode node, int pointValue) {
            if (node == null)
                return null;
            if (pointValue < node.pointValue)
                return getNode(node.left, pointValue);
            else if (pointValue > node.pointValue)
                return getNode(node.right, pointValue);
            else
                return node;
        }

        private TreeNode insert(TreeNode node, int pointValue, long count) {
            if (node == null)
                return new TreeNode(pointValue, count);
            if (pointValue < node.pointValue)
                node.left = insert(node.left, pointValue, count);
            else if (pointValue > node.pointValue)
                node.right = insert(node.right, pointValue, count);
            else
                node.count = count;
            node.height = 1 + Math.max(height(node.left), height(node.right));
            node.size = getSize(node.left) + getSize(node.right) + node.count;
            return balance(node);
        }

        private TreeNode deleteNode(TreeNode node, int pointValue) {
            if (node == null)
                return node;
            if (pointValue < node.pointValue)
                node.left = deleteNode(node.left, pointValue);
            else if (pointValue > node.pointValue)
                node.right = deleteNode(node.right, pointValue);
            else {
                if (node.left == null || node.right == null) {
                    TreeNode temp = (node.left != null) ? node.left : node.right;
                    if (temp == null) {
                        temp = node;
                        node = null;
                    } else
                        node = temp;
                } else {
                    TreeNode temp = minValueNode(node.right);
                    node.pointValue = temp.pointValue;
                    node.count = temp.count;
                    node.right = deleteNode(node.right, temp.pointValue);
                }
            }
            if (node == null)
                return node;
            node.height = 1 + Math.max(height(node.left), height(node.right));
            node.size = getSize(node.left) + getSize(node.right) + node.count;
            return balance(node);
        }

        private TreeNode minValueNode(TreeNode node) {
            TreeNode current = node;
            while (current.left != null)
                current = current.left;
            return current;
        }

        private int height(TreeNode node) {
            return node == null ? 0 : node.height;
        }

        private long getSize(TreeNode node) {
            return node == null ? 0 : node.size;
        }

        private TreeNode balance(TreeNode node) {
            int balanceFactor = getBalance(node);
            if (balanceFactor > 1) {
                if (getBalance(node.left) >= 0) {
                    return rightRotate(node);
                } else {
                    node.left = leftRotate(node.left);
                    return rightRotate(node);
                }
            }
            if (balanceFactor < -1) {
                if (getBalance(node.right) <= 0) {
                    return leftRotate(node);
                } else {
                    node.right = rightRotate(node.right);
                    return leftRotate(node);
                }
            }
            return node;
        }

        private int getBalance(TreeNode node) {
            return node == null ? 0 : height(node.left) - height(node.right);
        }

        private TreeNode rightRotate(TreeNode y) {
            TreeNode x = y.left;
            TreeNode T2 = x.right;

            x.right = y;
            y.left = T2;

            y.height = Math.max(height(y.left), height(y.right)) + 1;
            y.size = getSize(y.left) + getSize(y.right) + y.count;
            x.height = Math.max(height(x.left), height(x.right)) + 1;
            x.size = getSize(x.left) + getSize(x.right) + x.count;

            return x;
        }

        private TreeNode leftRotate(TreeNode x) {
            TreeNode y = x.right;
            TreeNode T2 = y.left;

            y.left = x;
            x.right = T2;

            x.height = Math.max(height(x.left), height(x.right)) + 1;
            x.size = getSize(x.left) + getSize(x.right) + x.count;
            y.height = Math.max(height(y.left), height(y.right)) + 1;
            y.size = getSize(y.left) + getSize(y.right) + y.count;

            return y;
        }

        public Integer findKthSmallest(long k) {
            return findKthSmallest(root, k);
        }

        private Integer findKthSmallest(TreeNode node, long k) {
            if (node == null)
                return null;
            long leftSize = getSize(node.left);
            if (k <= leftSize)
                return findKthSmallest(node.left, k);
            else if (k <= leftSize + node.count)
                return node.pointValue;
            else
                return findKthSmallest(node.right, k - leftSize - node.count);
        }

        public long countGreaterThan(int pointValue) {
            return countGreaterThan(root, pointValue);
        }

        private long countGreaterThan(TreeNode node, int pointValue) {
            if (node == null)
                return 0;
            if (pointValue < node.pointValue)
                return getSize(node.right) + node.count + countGreaterThan(node.left, pointValue);
            else if (pointValue == node.pointValue)
                return getSize(node.right);
            else
                return countGreaterThan(node.right, pointValue);
        }

        public long countLessThan(int pointValue) {
            return countLessThan(root, pointValue);
        }

        private long countLessThan(TreeNode node, int pointValue) {
            if (node == null)
                return 0;
            if (pointValue > node.pointValue)
                return getSize(node.left) + node.count + countLessThan(node.right, pointValue);
            else if (pointValue == node.pointValue)
                return getSize(node.left);
            else
                return countLessThan(node.left, pointValue);
        }
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
                String s = nextLine();
                if (s == null)
                    return null;
                tokenizer = new StringTokenizer(s);
            }
            return tokenizer.nextToken();
        }

        public int nextInt() {
            String s = next();
            return s == null ? -1 : Integer.parseInt(s);
        }
    }
}
