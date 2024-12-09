import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class TP2test12 {
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
    }

    // Update the Team class
    static class Team {
        int id;
        MyLinkedList<Participant> participants;
        int totalPoints;
        int memberCount;
        int uniquePointCount;
        MyTreeMap<Integer, Long> pointCounts;
        int timesCaught;
        MyTreeMap<ParticipantKey, Participant> participantTree;

        public Team(int id) {
            this.id = id;
            this.participants = new MyLinkedList<>();
            this.totalPoints = 0;
            this.memberCount = 0;
            this.uniquePointCount = 0;
            this.pointCounts = new MyTreeMap<>();
            this.timesCaught = 0;
            this.participantTree = new MyTreeMap<>();
        }
    }


    static MyTreeMap<Integer, Object> participants = new MyTreeMap<>();
    static MyTreeMap<Integer, Team> teams = new MyTreeMap<>();
    static MyLinkedList<Team> teamList = new MyLinkedList<>();

    static int participantIdCounter = 1;
    static int teamIdCounter = 1;

    public static void main(String[] args) throws IOException {
        InputStream inputStream = System.in;
        OutputStream outputStream = System.out;
        InputReader in = new InputReader(inputStream);
        out = new PrintWriter(outputStream);

        int M = in.nextInteger();
        int[] Mi = new int[M];
        for (int i = 0; i < M; i++) {
            Mi[i] = in.nextInteger();
        }

        Team jokiTeam = null;
        Team sofitaTeam = null;

        for (int i = 0; i < M; i++) {
            int MiValue = Mi[i];
            Team team = new Team(teamIdCounter);

            for (int j = 0; j < MiValue; j++) {
                int points = in.nextInteger();
                Participant participant = new Participant(participantIdCounter++, points, team);
                participants.put(participant.id, participant);
                team.participants.add(participant);
                team.totalPoints += points;
                team.memberCount++;

                // Check if the points are already in pointCounts
                Long currentCount = team.pointCounts.get(points);

                // If the points do not exist or the count is zero, increment uniquePointCount
                if (currentCount == null || currentCount == 0) {
                    team.uniquePointCount++;
                }
                team.pointCounts.put(points, currentCount == null ? 1L : currentCount + 1);

                // Add participant to participantTree
                team.participantTree.put(new ParticipantKey(participant), participant);
            }

            // team.uniquePointCount = team.pointCounts.size();

            teams.put(team.id, team);
            teamList.add(team);
            if (team.id == 1) {
                sofitaTeam = team;
            }
            teamIdCounter++;
        }

        // Mencari tim dengan poin paling rendah (tempat joki)
        jokiTeam = getTeamWithLowestPointsExcept(teamList, sofitaTeam);

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

            if (command == 'A'){
                // Parse the number of new participants
                long jumlahPesertaBaru = Long.parseLong(parts[1]);

                for (int i = 0; i < jumlahPesertaBaru; i++) {
                    Participant participant = new Participant(participantIdCounter++, 3, sofitaTeam);
                    participants.put(participant.id, participant);
                    sofitaTeam.participants.add(participant);
                    sofitaTeam.totalPoints += 3;
                    sofitaTeam.memberCount++;

                    if (!sofitaTeam.pointCounts.containsKey(3) || sofitaTeam.pointCounts.get(3) == 0) {
                        sofitaTeam.uniquePointCount++;
                    }
                    sofitaTeam.pointCounts.put(3, sofitaTeam.pointCounts.getOrDefault(3, 0L) + 1);

                    // Add participant to participantTree
                    sofitaTeam.participantTree.put(new ParticipantKey(participant), participant);
                }

                // Print the updated member count
                out.println(sofitaTeam.memberCount);
            }
            else if (command == 'B'){
                String boundType = parts[1];

                    int K = sofitaTeam.participants.size();
                    int[] pointsArray = new int[K];
                    for (int i = 0; i < K; i++) {
                        pointsArray[i] = sofitaTeam.participants.get(i).points;
                    }

                    // Sort the pointsArray
                    mergeSort(pointsArray, 0, K - 1);

                    int indexQ1 = Math.max(0, (int) Math.floor(0.25 * (K - 1)));
                    int indexQ3 = Math.min(K - 1, (int) Math.floor(0.75 * (K - 1)));

                    int Q1 = pointsArray[indexQ1];
                    int Q3 = pointsArray[indexQ3];
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
            }
            else if (command == 'M'){
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
                if (sofitaTeam!=null){
                    out.println(sofitaTeam.id);
                } else{
                    out.println(-1);
                }
            }
            else if (command == 'T'){
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
            }
            else if (command == 'G'){
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
                    if (!newTeam.pointCounts.containsKey(points) || newTeam.pointCounts.get(points) == 0) {
                        newTeam.uniquePointCount++;
                    }
                    newTeam.pointCounts.put(points, newTeam.pointCounts.getOrDefault(points, 0L) + 1);
            
                    // Add participant to participantTree
                    newTeam.participantTree.put(new ParticipantKey(participant), participant);
                }
            
                int sofitaIndex = teamList.indexOf(sofitaTeam);
                if (directionG.equals("L")) {
                    teamList.add(sofitaIndex, newTeam);
                } else if (directionG.equals("R")){
                    teamList.add(sofitaIndex+1, newTeam);
                }
            
                teams.put(newTeam.id, newTeam);
                teamIdCounter++;
                out.println(newTeam.id);
            } 
            else if (command == 'V'){
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
            }
            else if (command == 'E'){
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
            }            
            else if (command == 'U'){
                out.println(sofitaTeam.uniquePointCount);
            }
            else if (command == 'R'){
                mergeSortTeams(teamList, 0, teamList.size() - 1);
                if (!teamList.isEmpty()) {
                    sofitaTeam = teamList.get(0);
                    // System.out.println("cetak id sofita = " + sofitaTeam.id);
                    // System.out.println("cetak id joki = " + jokiTeam.id);
                    // printTeamList();
                
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
                // printTeamList();
                // System.out.println("sofita team = " + sofitaTeam.id);
                // System.out.println("joki team = " + jokiTeam.id);
            }
            else if (command == 'J'){
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

    private static void removeTopParticipants(Team team, MyTreeMap<Integer, Object> participants, int numParticipants) {
        // Get the top participants based on the criteria
        List<MyTreeMap.TreeNode<ParticipantKey, Participant>> topNodes = team.participantTree.getTopK(numParticipants);
        // System.out.println("team point counts before = "+ team.totalPoints);
        for (MyTreeMap.TreeNode<ParticipantKey, Participant> node : topNodes) {
            Participant p = node.value;
            // System.out.println("top participant to removed points = "+ p.points);
    
            // Remove participant from participantTree
            team.participantTree.remove(new ParticipantKey(p));
    
            // Remove participant from team.participants (the linked list)
            team.participants.remove(p);
    
            // Update team stats
            team.totalPoints -= p.points;
            team.memberCount--;
    
            // Update pointCounts and uniquePointCount
            Long count = team.pointCounts.get(p.points);
            if (count != null) {
                if (count == 1) {
                    team.pointCounts.remove(p.points);
                    team.uniquePointCount--;
                } else {
                    team.pointCounts.put(p.points, count - 1);
                }
            }
            // System.out.println("team point counts after = "+ team.totalPoints);
            
            // Remove participant from participantTree
            team.participantTree.remove(new ParticipantKey(p));
    
            // Remove participant from the global participants map
            participants.remove(p.id);
        }
    }
    
    private static void setAllParticipantsPointsToOne(Team team) {
        team.totalPoints = team.memberCount; // Since all points will be 1
        team.pointCounts = new MyTreeMap<>();
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

    private static void eliminateTeam(Team team, MyLinkedList<Team> teamList, MyTreeMap<Integer, Team> teams, MyTreeMap<Integer, Object> participants) {
        // Remove team from teamList
        teamList.remove(team);
    
        // Remove team from teams map
        teams.remove(team.id);
    
        // Collect participants to remove
        List<Participant> participantsToRemove = new ArrayList<>();
        MyLinkedList.Node<Participant> currentNode = team.participants.head;
        while (currentNode != null) {
            participantsToRemove.add(currentNode.data);
            currentNode = currentNode.next;
        }
    
        // Remove each participant
        for (Participant p : participantsToRemove) {
            eliminateParticipant(p, team);
        }
    
        // Clear team's participant list and reset statistics
        team.participants = new MyLinkedList<>();
        team.memberCount = 0;
        team.totalPoints = 0;
        team.pointCounts = new MyTreeMap<>();
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
        Object obj = participants.get(id);
        if (obj instanceof Participant) {
            return (Participant) obj;
        }
        return null;
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
        Long count = team.pointCounts.get(participant.points);
        if (count != null) {
            if (count == 1) {
                team.pointCounts.remove(participant.points);
                team.uniquePointCount--;
            } else {
                team.pointCounts.put(participant.points, count - 1);
            }
        }
    
        // Note: No need to adjust team.totalPoints here, as it's already adjusted in updateParticipantPoints
    }

    private static void updateParticipantPoints(Participant participant, int pointChange) {
        Team team = participant.team;
    
        // Remove old key from participantTree
        ParticipantKey oldKey = new ParticipantKey(participant);
        team.participantTree.remove(oldKey);
    
        // Update pointCounts and uniquePointCount for old points
        Long oldCount = team.pointCounts.get(participant.points);
        if (oldCount != null) {
            if (oldCount == 1) {
                team.pointCounts.remove(participant.points);
                team.uniquePointCount--;
            } else {
                team.pointCounts.put(participant.points, oldCount - 1);
            }
        }
    
        // Update participant's points
        participant.points += pointChange;
    
        // Update team's totalPoints
        team.totalPoints += pointChange;
    
        if (participant.points > 0) {
            // Update pointCounts and uniquePointCount for new points
            Long newCount = team.pointCounts.get(participant.points);
            if (newCount == null || newCount == 0) {
                team.uniquePointCount++;
            }
            team.pointCounts.put(participant.points, (newCount == null ? 1 : newCount + 1));
    
            // Re-insert participant into participantTree with updated points
            ParticipantKey newKey = new ParticipantKey(participant);
            team.participantTree.put(newKey, participant);
        } else {
            // Participant's points have dropped to zero or negative; eliminate them
            eliminateParticipant(participant, team);
        }
    }

    public static void mergeSort(int[] arr, int left, int right) {
        if (left < right) {
            int mid = (left + right) / 2;
            mergeSort(arr, left, mid);
            mergeSort(arr, mid + 1, right);
            merge(arr, left, mid, right);
        }
    }

    public static void merge(int[] arr, int left, int mid, int right) {
        int n1 = mid - left + 1;
        int n2 = right - mid;

        int[] leftArray = new int[n1];
        int[] rightArray = new int[n2];

        for (int i = 0; i < n1; ++i)
            leftArray[i] = arr[left + i];
        for (int j = 0; j < n2; ++j)
            rightArray[j] = arr[mid + 1 + j];

        int i = 0, j = 0, k = left;
        while (i < n1 && j < n2) {
            if (leftArray[i] <= rightArray[j]) {
                arr[k] = leftArray[i];
                i++;
            } else {
                arr[k] = rightArray[j];
                j++;
            }
            k++;
        }
        while (i < n1) {
            arr[k] = leftArray[i];
            i++;
            k++;
        }
        while (j < n2) {
            arr[k] = rightArray[j];
            j++;
            k++;
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
    
    private static void printTeamList() {
        System.out.println("Team list after sorting:");
        for (int i = 0; i < teamList.size(); i++) {
            Team team = teamList.get(i);
            System.out.println("Index " + i + ": Team ID: " + team.id + ", Total Points: " + team.totalPoints + ", Member Count: " + team.memberCount);
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
        private Node<E> head;
        private Node<E> tail;
        private int size;

        private static class Node<E> {
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

        public E remove(int index) {
            if (index < 0 || index >= size)
                throw new IndexOutOfBoundsException();
            Node<E> current = getNode(index);
            E data = current.data;
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
            return data;
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

        public boolean contains(E element) {
            return indexOf(element) != -1;
        }

        public boolean isEmpty() {
            return size == 0;
        }
    }

    // Custom TreeMap implementation using AVL Tree
    static class MyTreeMap<K extends Comparable<K>, V> {
        private TreeNode<K, V> root;

        private static class TreeNode<K, V> {
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

        // New getOrDefault method
        public V getOrDefault(K key, V defaultValue) {
            TreeNode<K, V> node = getNode(root, key);
            return (node == null) ? defaultValue : node.value;
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
                    TreeNode<K, V> temp = null;
                    if (temp == root.left)
                        temp = root.right;
                    else
                        temp = root.left;
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
            root.height = Math.max(height(root.left), height(root.right)) + 1;
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

        public List<TreeNode<K, V>> getTopK(int k) {
            List<TreeNode<K, V>> result = new ArrayList<>();
            getTopK(root, result, k);
            return result;
        }
        
        private void getTopK(TreeNode<K, V> node, List<TreeNode<K, V>> result, int k) {
            if (node == null || result.size() >= k)
                return;
            getTopK(node.left, result, k); // Visit left subtree first
            if (result.size() < k)
                result.add(node);
            getTopK(node.right, result, k); // Then right subtree
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
                try {
                    tokenizer = new StringTokenizer(reader.readLine());
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
