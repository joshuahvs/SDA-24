import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class TPtest21 {
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

    // KeyValuePair class to hold key-value pairs in the AVL tree
    static class KeyValuePair<K extends Comparable<K>, V> implements Comparable<KeyValuePair<K, V>> {
        K key;
        V value;

        public KeyValuePair(K key, V value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public int compareTo(KeyValuePair<K, V> other) {
            return this.key.compareTo(other.key);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (!(obj instanceof KeyValuePair))
                return false;
            KeyValuePair<?, ?> other = (KeyValuePair<?, ?>) obj;
            return this.key.equals(other.key);
        }

        @Override
        public int hashCode() {
            return key.hashCode();
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

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (!(obj instanceof ParticipantKey))
                return false;
            ParticipantKey other = (ParticipantKey) obj;
            return this.participant.id == other.participant.id;
        }

        @Override
        public int hashCode() {
            return Integer.hashCode(participant.id);
        }
    }

    // Update the Team class
    static class Team {
        int id;
        MyLinkedList<Participant> participants;
        int totalPoints;
        int memberCount;
        int uniquePointCount;
        MyAVLTree<KeyValuePair<Integer, Long>> pointCounts;
        int timesCaught;
        MyAVLTree<KeyValuePair<ParticipantKey, Participant>> participantTree;

        public Team(int id) {
            this.id = id;
            this.participants = new MyLinkedList<>();
            this.totalPoints = 0;
            this.memberCount = 0;
            this.uniquePointCount = 0;
            this.pointCounts = new MyAVLTree<>();
            this.timesCaught = 0;
            this.participantTree = new MyAVLTree<>();
        }
    }

    // Global AVL Trees as Key-Value Pairs
    static MyAVLTree<KeyValuePair<Integer, Participant>> participants = new MyAVLTree<>();
    static MyAVLTree<KeyValuePair<Integer, Team>> teams = new MyAVLTree<>();
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
                participants.insert(new KeyValuePair<>(participant.id, participant));
                team.participants.add(participant);
                team.totalPoints += points;
                team.memberCount++;

                // Check if the points are already in pointCounts
                KeyValuePair<Integer, Long> pointPair = new KeyValuePair<>(points, 0L);
                KeyValuePair<Integer, Long> existingPair = team.pointCounts.find(pointPair);
                if (existingPair == null) {
                    team.uniquePointCount++;
                    team.pointCounts.insert(new KeyValuePair<>(points, 1L));
                } else {
                    existingPair.value += 1;
                    // Update the existing pair by removing and re-inserting
                    team.pointCounts.remove(existingPair);
                    team.pointCounts.insert(existingPair);
                }

                // Add participant to participantTree
                team.participantTree.insert(new KeyValuePair<>(new ParticipantKey(participant), participant));
            }

            teams.insert(new KeyValuePair<>(team.id, team));
            teamList.add(team);
            if (team.id == 1) {
                sofitaTeam = team;
            }
            teamIdCounter++;
        }

        // After consequences are applied
        jokiTeam = getTeamWithLowestPoints(teamList, sofitaTeam);

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

            if (command == 'A') {
                // Parse the number of new participants
                long jumlahPesertaBaru = Long.parseLong(parts[1]);
                if (sofitaTeam != null) {
                    for (int i = 0; i < jumlahPesertaBaru; i++) {
                        Participant participant = new Participant(participantIdCounter++, 3, sofitaTeam);
                        participants.insert(new KeyValuePair<>(participant.id, participant));
                        sofitaTeam.participants.add(participant);
                        sofitaTeam.totalPoints += 3;
                        sofitaTeam.memberCount++;

                        // Update pointCounts
                        KeyValuePair<Integer, Long> pointPair = new KeyValuePair<>(3, 0L);
                        KeyValuePair<Integer, Long> existingPair = sofitaTeam.pointCounts.find(pointPair);
                        if (existingPair == null) {
                            sofitaTeam.uniquePointCount++;
                            sofitaTeam.pointCounts.insert(new KeyValuePair<>(3, 1L));
                        } else {
                            existingPair.value += 1;
                            sofitaTeam.pointCounts.remove(existingPair);
                            sofitaTeam.pointCounts.insert(existingPair);
                        }

                        // Add participant to participantTree
                        sofitaTeam.participantTree.insert(new KeyValuePair<>(new ParticipantKey(participant), participant));
                    }

                    // Print the updated member count
                    out.println(sofitaTeam.memberCount);
                } else {
                    out.println(-1);
                }
            } else if (command == 'B') {
                String boundType = parts[1];

                if (sofitaTeam != null) {
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
                } else {
                    out.println(-1);
                }
            } else if (command == 'M') {
                String direction = parts[1];
                int sofitaIndex = teamList.indexOf(sofitaTeam);
                if (teamList.size() > 0) {
                    if (direction.equals("L")) {
                        sofitaIndex = (sofitaIndex - 1 + teamList.size()) % teamList.size();
                    } else if (direction.equals("R")) {
                        sofitaIndex = (sofitaIndex + 1) % teamList.size();
                    }
                    sofitaTeam = teamList.get(sofitaIndex);
                } else {
                    continue;
                }
                do {
                    // Check if Joki is in the same team
                    if (jokiTeam != null && sofitaTeam.id == jokiTeam.id) {
                        // Apply consequences based on timesCaught
                        sofitaTeam.timesCaught++;
                        if (sofitaTeam.timesCaught == 1) {
                            // First time caught: Remove top 3 participants
                            removeTopParticipants(sofitaTeam, 3);
                        } else if (sofitaTeam.timesCaught == 2) {
                            // Second time caught: Set all participants' points to one
                            setAllParticipantsPointsToOne(sofitaTeam);
                        } else if (sofitaTeam.timesCaught >= 3) {
                            // Third time caught: Eliminate the team
                            eliminateTeam(sofitaTeam);
                            // Move Sofita to team with highest total points
                            if (!teamList.isEmpty()) {
                                sofitaTeam = getTeamWithHighestPoints(teamList);
                            } else {
                                sofitaTeam = null;
                            }
                        }
                        // Joki is expelled and moves to team with lowest total points
                        jokiTeam = getTeamWithLowestPoints(teamList, sofitaTeam);
                    }

                    if (sofitaTeam != null && sofitaTeam.memberCount < 7) {
                        eliminateTeam(sofitaTeam);
                        // Move Sofita to the team with highest total points
                        if (!teamList.isEmpty()) {
                            sofitaTeam = getTeamWithHighestPoints(teamList);
                        } else {
                            sofitaTeam = null;
                        }
                    }
                } while (sofitaTeam != null && jokiTeam != null && sofitaTeam.id == jokiTeam.id);

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

                if (sender == null || receiver == null) {
                    out.println(-1);
                } else if (sender.team != sofitaTeam || receiver.team != sofitaTeam) {
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
                        eliminateTeam(sender.team);
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
                    participants.insert(new KeyValuePair<>(participant.id, participant));
                    newTeam.participants.add(participant);
                    newTeam.totalPoints += points; // Correctly add 1 to totalPoints
                    newTeam.memberCount++;

                    // Update uniquePointCount and pointCounts correctly
                    KeyValuePair<Integer, Long> pointPair = new KeyValuePair<>(points, 0L);
                    KeyValuePair<Integer, Long> existingPair = newTeam.pointCounts.find(pointPair);
                    if (existingPair == null) {
                        newTeam.uniquePointCount++;
                        newTeam.pointCounts.insert(new KeyValuePair<>(points, 1L));
                    } else {
                        existingPair.value += 1;
                        newTeam.pointCounts.remove(existingPair);
                        newTeam.pointCounts.insert(existingPair);
                    }

                    // Add participant to participantTree
                    newTeam.participantTree.insert(new KeyValuePair<>(new ParticipantKey(participant), participant));
                }

                int sofitaIndex = teamList.indexOf(sofitaTeam);
                if (directionG.equals("L")) {
                    teamList.add(sofitaIndex, newTeam);
                } else if (directionG.equals("R")) {
                    teamList.add(sofitaIndex + 1, newTeam);
                }

                teams.insert(new KeyValuePair<>(newTeam.id, newTeam));
                teamIdCounter++;
                out.println(newTeam.id);
            } else if (command == 'V') {
                int participant1Id = Integer.parseInt(parts[1]);
                int participant2Id = Integer.parseInt(parts[2]);
                int opponentTeamId = Integer.parseInt(parts[3]);
                int result = Integer.parseInt(parts[4]);

                Participant participant1 = getParticipantById(participant1Id);
                Team opponentTeam = getTeamById(opponentTeamId);
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

                        // Check if participants's team needs to be eliminated
                        if (participant2.team.memberCount < 7) {
                            eliminateTeam(participant2.team);
                            // Move Sofita to the team with highest total points
                            if (!teamList.isEmpty()) {
                                sofitaTeam = getTeamWithHighestPoints(teamList);
                            } else {
                                sofitaTeam = null;
                            }
                        }
                        out.println(participant1.points);
                    } else if (result == -1) {
                        // Participant2 wins
                        updateParticipantPoints(participant1, -3);
                        updateParticipantPoints(participant2, 3);

                        // Check if participants's team needs to be eliminated
                        if (participant1.team.memberCount < 7) {
                            eliminateTeam(participant1.team);
                            // Move Sofita to the team with highest total points
                            if (!teamList.isEmpty()) {
                                sofitaTeam = getTeamWithHighestPoints(teamList);
                            } else {
                                sofitaTeam = null;
                            }
                        }
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
                    eliminateTeam(team);

                    if (team == sofitaTeam) {
                        sofitaTeamEliminated = true;
                    }
                    if (team == jokiTeam) {
                        jokiTeam = getTeamWithLowestPoints(teamList, sofitaTeam);
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
                if (sofitaTeam != null) {
                    out.println(sofitaTeam.uniquePointCount);
                } else {
                    out.println(-1);
                }
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
                            removeTopParticipants(sofitaTeam, 3);
                        } else if (sofitaTeam.timesCaught == 2) {
                            // Second time caught: Set all participants' points to one
                            setAllParticipantsPointsToOne(sofitaTeam);
                        } else if (sofitaTeam.timesCaught >= 3) {
                            // Third time caught: Eliminate the team
                            eliminateTeam(sofitaTeam);
                            // Move Sofita to team with highest total points
                            if (!teamList.isEmpty()) {
                                sofitaTeam = teamList.get(0);
                            } else {
                                sofitaTeam = null;
                            }
                        }
                        // Joki is expelled and moves to team with lowest total points
                        jokiTeam = getTeamWithLowestPoints(teamList, sofitaTeam);
                    }

                    out.println(sofitaTeam != null ? sofitaTeam.id : -1);
                } else {
                    out.println(-1);
                }
            } else if (command == 'J') {
                String jokiDirection = parts[1];
                int jokiIndex = teamList.indexOf(jokiTeam);
                if (teamList.size() != 0) {
                    if (jokiDirection.equals("L")) {
                        jokiIndex = (jokiIndex - 1 + teamList.size()) % teamList.size();
                    } else if (jokiDirection.equals("R")) {
                        jokiIndex = (jokiIndex + 1) % teamList.size();
                    }
                    Team destinationTeam = teamList.get(jokiIndex);
                    if (destinationTeam.id != sofitaTeam.id) {
                        jokiTeam = teamList.get(jokiIndex);
                    }
                    if (jokiTeam != null) {
                        out.println(jokiTeam.id);
                    } else {
                        out.println(-1);
                    }
                } else {
                    out.println(-1);
                }
            }
        }
        out.close();
    }

    // Helper method to get a team by its ID
    private static Team getTeamById(int id) {
        KeyValuePair<Integer, Team> searchPair = new KeyValuePair<>(id, null);
        KeyValuePair<Integer, Team> found = teams.find(searchPair);
        return found != null ? found.value : null;
    }

    // Helper methods to operate on MyAVLTree<KeyValuePair<K, V>>
    private static <K extends Comparable<K>, V> void put(MyAVLTree<KeyValuePair<K, V>> tree, K key, V value) {
        KeyValuePair<K, V> pair = new KeyValuePair<>(key, value);
        KeyValuePair<K, V> existing = tree.find(pair);
        if (existing != null) {
            tree.remove(existing);
        }
        tree.insert(pair);
    }

    private static <K extends Comparable<K>, V> V get(MyAVLTree<KeyValuePair<K, V>> tree, K key) {
        KeyValuePair<K, V> searchPair = new KeyValuePair<>(key, null);
        KeyValuePair<K, V> found = tree.find(searchPair);
        return found != null ? found.value : null;
    }

    private static <K extends Comparable<K>, V> void remove(MyAVLTree<KeyValuePair<K, V>> tree, K key) {
        KeyValuePair<K, V> searchPair = new KeyValuePair<>(key, null);
        tree.remove(searchPair);
    }

    private static Team getTeamWithLowestPoints(MyLinkedList<Team> teamList, Team excludeTeam) {
        Team bestTeam = null;

        for (int i = 0; i < teamList.size(); i++) {
            Team team = teamList.get(i);
            if (team == excludeTeam) {
                continue; // Exclude the specified team
            }

            if (bestTeam == null ||
                team.totalPoints < bestTeam.totalPoints ||
                (team.totalPoints == bestTeam.totalPoints && team.memberCount > bestTeam.memberCount) ||
                (team.totalPoints == bestTeam.totalPoints && team.memberCount == bestTeam.memberCount && team.id > bestTeam.id)) {

                // Update the best team if it satisfies the conditions
                bestTeam = team;
            }
        }

        return bestTeam; // Return the best team found
    }

    private static void removeTopParticipants(Team team, int numParticipants) {
        // Retrieve top K participants from participantTree
        List<KeyValuePair<ParticipantKey, Participant>> topParticipants = team.participantTree.getTopK(numParticipants);
        for (KeyValuePair<ParticipantKey, Participant> pair : topParticipants) {
            Participant p = pair.value;

            // Remove from participantTree
            team.participantTree.remove(pair);

            // Remove from team participants list
            team.participants.remove(p);

            // Update team stats
            team.totalPoints -= p.points;
            team.memberCount--;

            // Update pointCounts
            KeyValuePair<Integer, Long> pointPair = new KeyValuePair<>(p.points, 0L);
            KeyValuePair<Integer, Long> existingPair = team.pointCounts.find(pointPair);
            if (existingPair != null) {
                if (existingPair.value == 1) {
                    team.pointCounts.remove(existingPair);
                    team.uniquePointCount--;
                } else {
                    existingPair.value -= 1;
                    team.pointCounts.remove(existingPair);
                    team.pointCounts.insert(existingPair);
                }
            }

            // Remove from global participants map
            remove(participants, p.id);
        }
    }

    private static void setAllParticipantsPointsToOne(Team team) {
        team.totalPoints = team.memberCount; // Since all points will be 1
        team.pointCounts = new MyAVLTree<>();
        team.uniquePointCount = 1; // Only one unique point value now
        team.pointCounts.insert(new KeyValuePair<>(1, (long) team.memberCount));

        // Clear participantTree and rebuild it
        team.participantTree = new MyAVLTree<>();

        MyLinkedList.Node<Participant> currentNode = team.participants.head;
        while (currentNode != null) {
            Participant p = currentNode.data;
            p.points = 1;
            // Create new ParticipantKey
            ParticipantKey key = new ParticipantKey(p);
            // Insert into participantTree
            team.participantTree.insert(new KeyValuePair<>(key, p));

            currentNode = currentNode.next;
        }
    }

    private static void eliminateTeam(Team team) {
        // Remove team from teamList
        teamList.remove(team);

        // Remove team from teams map
        remove(teams, team.id);

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
        team.pointCounts = new MyAVLTree<>();
        team.uniquePointCount = 0;
        team.participantTree = new MyAVLTree<>();
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
        KeyValuePair<Integer, Participant> searchPair = new KeyValuePair<>(id, null);
        KeyValuePair<Integer, Participant> found = participants.find(searchPair);
        return found != null ? found.value : null;
    }

    private static void eliminateParticipant(Participant participant, Team team) {
        // Remove participant from participants map
        remove(participants, participant.id);

        // Remove participant from team's participant list
        team.participants.remove(participant);

        // Update team member count
        team.memberCount--;

        // Remove participant from participantTree
        ParticipantKey key = new ParticipantKey(participant);
        KeyValuePair<ParticipantKey, Participant> searchPair = new KeyValuePair<>(key, participant);
        team.participantTree.remove(searchPair);

        // Update pointCounts and uniquePointCount
        KeyValuePair<Integer, Long> pointPair = new KeyValuePair<>(participant.points, 0L);
        KeyValuePair<Integer, Long> existingPair = team.pointCounts.find(pointPair);
        if (existingPair != null) {
            if (existingPair.value == 1) {
                team.pointCounts.remove(existingPair);
                team.uniquePointCount--;
            } else {
                existingPair.value -= 1;
                team.pointCounts.remove(existingPair);
                team.pointCounts.insert(existingPair);
            }
        }

        // Note: No need to adjust team.totalPoints here, as it's already adjusted in updateParticipantPoints
    }

    private static void updateParticipantPoints(Participant participant, int pointChange) {
        Team team = participant.team;

        // Remove old key from participantTree
        ParticipantKey oldKey = new ParticipantKey(participant);
        KeyValuePair<ParticipantKey, Participant> oldPair = new KeyValuePair<>(oldKey, participant);
        team.participantTree.remove(oldPair);

        // Update pointCounts and uniquePointCount for old points
        KeyValuePair<Integer, Long> oldPointPair = new KeyValuePair<>(participant.points, 0L);
        KeyValuePair<Integer, Long> existingOldPair = team.pointCounts.find(oldPointPair);
        if (existingOldPair != null) {
            if (existingOldPair.value == 1) {
                team.pointCounts.remove(existingOldPair);
                team.uniquePointCount--;
            } else {
                existingOldPair.value -= 1;
                team.pointCounts.remove(existingOldPair);
                team.pointCounts.insert(existingOldPair);
            }
        }

        // Update participant's points
        participant.points += pointChange;

        // Update team's totalPoints
        team.totalPoints += pointChange;

        if (participant.points > 0) {
            // Update pointCounts and uniquePointCount for new points
            KeyValuePair<Integer, Long> newPointPair = new KeyValuePair<>(participant.points, 0L);
            KeyValuePair<Integer, Long> existingNewPair = team.pointCounts.find(newPointPair);
            if (existingNewPair == null) {
                team.uniquePointCount++;
                team.pointCounts.insert(new KeyValuePair<>(participant.points, 1L));
            } else {
                existingNewPair.value += 1;
                team.pointCounts.remove(existingNewPair);
                team.pointCounts.insert(existingNewPair);
            }

            // Re-insert participant into participantTree with updated points
            ParticipantKey newKey = new ParticipantKey(participant);
            KeyValuePair<ParticipantKey, Participant> newPair = new KeyValuePair<>(newKey, participant);
            team.participantTree.insert(newPair);
        } else {
            // Participant's points have dropped to zero or negative; eliminate them
            eliminateParticipant(participant, team);
        }
    }

    // Merge Sort for integer arrays
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

    // Merge Sort for Teams based on custom comparison
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
        for (int i = 0; i < teamList.size(); i++) {
            Team team = teamList.get(i);
            out.println("Index " + i + ": Team ID: " + team.id + ", Total Points: " + team.totalPoints + ", Member Count: " + team.memberCount + ", Times Caught:" + team.timesCaught);
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

    // AVL Tree implementation with KeyValuePair
    static class MyAVLTree<K extends Comparable<K>> {
        private TreeNode<K> root;

        private static class TreeNode<K> {
            K key;
            TreeNode<K> left;
            TreeNode<K> right;
            int height;

            public TreeNode(K key) {
                this.key = key;
                this.height = 1; // New node starts with height 1
            }
        }

        // Insert a key into the AVL Tree
        public void insert(K key) {
            root = insertNode(root, key);
        }

        private TreeNode<K> insertNode(TreeNode<K> node, K key) {
            if (node == null) {
                return new TreeNode<>(key);
            }

            int cmp = key.compareTo(node.key);
            if (cmp < 0) {
                node.left = insertNode(node.left, key);
            } else if (cmp > 0) {
                node.right = insertNode(node.right, key);
            } else {
                // Duplicate keys are not allowed
                return node;
            }

            // Update height and balance the tree
            node.height = 1 + Math.max(height(node.left), height(node.right));
            return balance(node);
        }

        // Remove a key from the AVL Tree
        public void remove(K key) {
            root = removeNode(root, key);
        }

        private TreeNode<K> removeNode(TreeNode<K> node, K key) {
            if (node == null) {
                return null;
            }

            int cmp = key.compareTo(node.key);
            if (cmp < 0) {
                node.left = removeNode(node.left, key);
            } else if (cmp > 0) {
                node.right = removeNode(node.right, key);
            } else {
                // Node with one or no children
                if ((node.left == null) || (node.right == null)) {
                    TreeNode<K> temp = null;
                    if (temp == node.left)
                        temp = node.right;
                    else
                        temp = node.left;
                    if (temp == null) {
                        temp = node;
                        node = null;
                    } else
                        node = temp;
                } else {
                    // Node with two children: Get the in-order successor
                    TreeNode<K> temp = minValueNode(node.right);
                    node.key = temp.key;
                    node.right = removeNode(node.right, temp.key);
                }
            }

            // Update height and balance the tree
            if (node == null)
                return node;

            node.height = Math.max(height(node.left), height(node.right)) + 1;
            return balance(node);
        }

        // Find a key in the AVL Tree
        public boolean contains(K key) {
            return findNode(root, key) != null;
        }

        public K find(K key) {
            TreeNode<K> node = findNode(root, key);
            return node != null ? node.key : null;
        }

        private TreeNode<K> findNode(TreeNode<K> node, K key) {
            if (node == null) {
                return null;
            }
            int cmp = key.compareTo(node.key);
            if (cmp < 0) {
                return findNode(node.left, key);
            } else if (cmp > 0) {
                return findNode(node.right, key);
            } else {
                return node;
            }
        }

        // Helper methods for AVL Tree

        private int height(TreeNode<K> node) {
            return node == null ? 0 : node.height;
        }

        private TreeNode<K> balance(TreeNode<K> node) {
            int balanceFactor = getBalance(node);

            // Left-heavy
            if (balanceFactor > 1) {
                if (getBalance(node.left) >= 0) {
                    return rightRotate(node); // LL case
                } else {
                    node.left = leftRotate(node.left); // LR case
                    return rightRotate(node);
                }
            }

            // Right-heavy
            if (balanceFactor < -1) {
                if (getBalance(node.right) <= 0) {
                    return leftRotate(node); // RR case
                } else {
                    node.right = rightRotate(node.right); // RL case
                    return leftRotate(node);
                }
            }

            return node; // Already balanced
        }

        private int getBalance(TreeNode<K> node) {
            return node == null ? 0 : height(node.left) - height(node.right);
        }

        private TreeNode<K> rightRotate(TreeNode<K> y) {
            TreeNode<K> x = y.left;
            TreeNode<K> T2 = x.right;

            // Perform rotation
            x.right = y;
            y.left = T2;

            // Update heights
            y.height = Math.max(height(y.left), height(y.right)) + 1;
            x.height = Math.max(height(x.left), height(x.right)) + 1;

            return x;
        }

        private TreeNode<K> leftRotate(TreeNode<K> x) {
            TreeNode<K> y = x.right;
            TreeNode<K> T2 = y.left;

            // Perform rotation
            y.left = x;
            x.right = T2;

            // Update heights
            x.height = Math.max(height(x.left), height(x.right)) + 1;
            y.height = Math.max(height(y.left), height(y.right)) + 1;

            return y;
        }

        private TreeNode<K> minValueNode(TreeNode<K> node) {
            TreeNode<K> current = node;
            while (current.left != null) {
                current = current.left;
            }
            return current;
        }

        // Traverse the AVL Tree (in-order)
        public void inOrderTraversal() {
            inOrderTraversal(root);
            System.out.println();
        }

        private void inOrderTraversal(TreeNode<K> node) {
            if (node != null) {
                inOrderTraversal(node.left);
                System.out.print(node.key + " ");
                inOrderTraversal(node.right);
            }
        }

        // Get Top K elements (assuming in-order traversal gives sorted order)
        public List<K> getTopK(int k) {
            List<K> result = new ArrayList<>();
            getTopK(root, result, k);
            return result;
        }

        private void getTopK(TreeNode<K> node, List<K> result, int k) {
            if (node == null || result.size() >= k)
                return;
            // Assuming larger elements are on the right, traverse right first for descending order
            getTopK(node.right, result, k);
            if (result.size() < k)
                result.add(node.key);
            getTopK(node.left, result, k);
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
                    String line = reader.readLine();
                    if(line == null){
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
            String token = next();
            if(token == null){
                throw new RuntimeException("No more tokens");
            }
            return Integer.parseInt(token);
        }
    }
}
