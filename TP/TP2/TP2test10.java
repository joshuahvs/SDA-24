import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.StringTokenizer;
import java.util.Map;
import java.util.TreeMap;

public class TP2test10 {
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

    // ParticipantRange represents a batch of participants with the same points
    static class ParticipantRange {
        int startId;
        int endId;
        int points;
        Team team;

        public ParticipantRange(int startId, int endId, int points, Team team) {
            this.startId = startId;
            this.endId = endId;
            this.points = points;
            this.team = team;
        }

        public long size() {
            return (long) endId - startId + 1;
        }
    }

    static class Team {
        int id;
        MyLinkedList<Object> participants; // Can be Participant or ParticipantRange
        long totalPoints;
        long memberCount;
        int uniquePointCount;
        TreeMap<Integer, Long> pointCounts; // Map from points to count of participants
        int timesCaught;

        public Team(int id) {
            this.id = id;
            this.participants = new MyLinkedList<>();
            this.totalPoints = 0;
            this.memberCount = 0;
            this.uniquePointCount = 0;
            this.pointCounts = new TreeMap<>();
            this.timesCaught = 0;
        }
    }

    static TreeMap<Integer, Object> participants = new TreeMap<>();
    static TreeMap<Integer, Team> teams = new TreeMap<>();
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

                team.pointCounts.put(points, team.pointCounts.getOrDefault(points, 0L) + 1);
            }

            team.uniquePointCount = team.pointCounts.size();

            teams.put(team.id, team);
            teamList.add(team);
            if (team.id == 1) {
                sofitaTeam = team;
            }
            teamIdCounter++;
        }

        // Find the team with the lowest total points (excluding sofitaTeam)
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

            switch (command) {
                case 'A':
                    long jumlahPesertaBaru = Long.parseLong(parts[1]);

                    int startId = participantIdCounter;
                    participantIdCounter += jumlahPesertaBaru;
                    int endId = participantIdCounter - 1;

                    ParticipantRange participantRange = new ParticipantRange(startId, endId, 3, sofitaTeam);
                    participants.put(startId, participantRange);
                    sofitaTeam.participants.add(participantRange);

                    sofitaTeam.memberCount += jumlahPesertaBaru;
                    sofitaTeam.totalPoints += jumlahPesertaBaru * 3;

                    sofitaTeam.pointCounts.put(3, sofitaTeam.pointCounts.getOrDefault(3, 0L) + jumlahPesertaBaru);
                    sofitaTeam.uniquePointCount = sofitaTeam.pointCounts.size();

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

                    out.println(sofitaTeam != null ? sofitaTeam.id : -1);
                    break;
                case 'J':
                    if (jokiTeam == null) {
                        out.println(-1);
                        break;
                    }
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
                    break;
                case 'U':
                    out.println(sofitaTeam.uniquePointCount);
                    break;
                case 'G':
                    String directionG = parts[1];
                    Team newTeam = new Team(teamIdCounter++);
                    // Add seven new participants as a range
                    int startNewId = participantIdCounter;
                    participantIdCounter += 7;
                    int endNewId = participantIdCounter - 1;

                    ParticipantRange newParticipantRange = new ParticipantRange(startNewId, endNewId, 1, newTeam);
                    participants.put(startNewId, newParticipantRange);
                    newTeam.participants.add(newParticipantRange);

                    newTeam.memberCount += 7;
                    newTeam.totalPoints += 7 * 1;
                    newTeam.pointCounts.put(1, 7L);
                    newTeam.uniquePointCount = 1;

                    // Insert the new team to the left or right of Sofita's current team
                    int sofitaIndexG = teamList.indexOf(sofitaTeam);
                    if (directionG.equals("L")) {
                        teamList.add(sofitaIndexG, newTeam);
                    } else if (directionG.equals("R")) {
                        teamList.add(sofitaIndexG + 1, newTeam);
                    }
                    teams.put(newTeam.id, newTeam);

                    // Update Joki's team if necessary
                    if (jokiTeam == null) {
                        jokiTeam = getTeamWithLowestPointsExcept(teamList, sofitaTeam);
                    }

                    out.println(newTeam.id);
                    break;
                case 'T':
                    int senderId = Integer.parseInt(parts[1]);
                    int receiverId = Integer.parseInt(parts[2]);
                    int amount = Integer.parseInt(parts[3]);

                    Participant sender = getOrCreateParticipant(senderId, sofitaTeam);
                    Participant receiver = getOrCreateParticipant(receiverId, sofitaTeam);

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
                        out.println(sender.points + " " + receiver.points);
                    }
                    break;
                case 'V':
                    int participant1Id = Integer.parseInt(parts[1]);
                    int participant2Id = Integer.parseInt(parts[2]);
                    int opponentTeamId = Integer.parseInt(parts[3]);
                    int result = Integer.parseInt(parts[4]);

                    Participant participant1 = getOrCreateParticipant(participant1Id, sofitaTeam);
                    Team opponentTeam = teams.get(opponentTeamId);
                    Participant participant2 = getOrCreateParticipant(participant2Id, opponentTeam);

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
                    break;
                case 'E':
                    int threshold = Integer.parseInt(parts[1]);

                    MyLinkedList<Team> eliminatedTeams = new MyLinkedList<>();
                    boolean sofitaTeamEliminated = false;

                    // Using iterator to safely remove items during iteration
                    for (int i = 0; i < teamList.size();) {
                        Team team = teamList.get(i);
                        if (team.totalPoints < threshold) {
                            // Eliminate team
                            eliminatedTeams.add(team);
                            teamList.remove(i); // Remove from teamList
                            teams.remove(team.id);

                            // Remove participants
                            for (int j = 0; j < team.participants.size(); j++) {
                                Object obj = team.participants.get(j);
                                if (obj instanceof Participant) {
                                    Participant p = (Participant) obj;
                                    participants.remove(p.id);
                                } else if (obj instanceof ParticipantRange) {
                                    ParticipantRange pr = (ParticipantRange) obj;
                                    participants.remove(pr.startId);
                                }
                            }

                            if (team == sofitaTeam) {
                                sofitaTeamEliminated = true;
                            }
                            if (team == jokiTeam) {
                                jokiTeam = getTeamWithLowestPointsExcept(teamList, sofitaTeam);
                            }
                        } else {
                            i++;
                        }
                    }
                    // If Sofita's team was eliminated
                    if (sofitaTeamEliminated) {
                        if (!teamList.isEmpty()) {
                            // Find team with highest totalPoints
                            sofitaTeam = getTeamWithHighestPoints(teamList);
                        } else {
                            sofitaTeam = null;
                        }
                    }

                    out.println(eliminatedTeams.size());
                    break;
                case 'R':
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
                    break;
                case 'B':
                    String boundType = parts[1];

                    // Compute U and L without expanding participant ranges
                    // Build a map from points to counts
                    TreeMap<Integer, Long> pointCounts = new TreeMap<>(sofitaTeam.pointCounts);

                    long K = sofitaTeam.memberCount;
                    long[] cumulativeCounts = new long[pointCounts.size()];
                    int[] pointsArray = new int[pointCounts.size()];
                    int idx = 0;
                    long cumulative = 0;
                    for (Map.Entry<Integer, Long> entry : pointCounts.entrySet()) {
                        cumulative += entry.getValue();
                        cumulativeCounts[idx] = cumulative;
                        pointsArray[idx] = entry.getKey();
                        idx++;
                    }

                    // Find Q1 and Q3
                    int indexQ1 = Math.max(0, (int) Math.floor(0.25 * K) - 1);
                    int indexQ3 = Math.min((int) K - 1, (int) Math.floor(0.75 * K) - 1);

                    int Q1 = getPointAtIndex(cumulativeCounts, pointsArray, indexQ1);
                    int Q3 = getPointAtIndex(cumulativeCounts, pointsArray, indexQ3);
                    int IQR = Q3 - Q1;

                    int L = Q1 - (int) (1.5 * IQR);
                    int U = Q3 + (int) (1.5 * IQR);

                    long count = 0;
                    if (boundType.equals("U")) {
                        // Count participants with points > U
                        Map<Integer, Long> tailMap = pointCounts.tailMap(U + 1);
                        for (long c : tailMap.values()) {
                            count += c;
                        }
                    } else if (boundType.equals("L")) {
                        // Count participants with points < L
                        Map<Integer, Long> headMap = pointCounts.headMap(L);
                        for (long c : headMap.values()) {
                            count += c;
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

    private static int getPointAtIndex(long[] cumulativeCounts, int[] pointsArray, int index) {
        for (int i = 0; i < cumulativeCounts.length; i++) {
            if (cumulativeCounts[i] > index) {
                return pointsArray[i];
            }
        }
        // Should not reach here if index is within total participants
        return pointsArray[pointsArray.length - 1];
    }

    private static Participant getOrCreateParticipant(int participantId, Team team) {
        Map.Entry<Integer, Object> entry = participants.floorEntry(participantId);
        if (entry == null)
            return null;

        Object obj = entry.getValue();
        if (obj instanceof ParticipantRange) {
            ParticipantRange range = (ParticipantRange) obj;
            if (participantId >= range.startId && participantId <= range.endId) {
                // Split the range
                participants.remove(range.startId);
                team.participants.remove(range);

                if (participantId > range.startId) {
                    ParticipantRange leftRange = new ParticipantRange(range.startId, participantId - 1, range.points,
                            team);
                    participants.put(leftRange.startId, leftRange);
                    team.participants.add(leftRange);
                }

                if (participantId < range.endId) {
                    ParticipantRange rightRange = new ParticipantRange(participantId + 1, range.endId, range.points,
                            team);
                    participants.put(rightRange.startId, rightRange);
                    team.participants.add(rightRange);
                }

                // Create the individual participant
                Participant participant = new Participant(participantId, range.points, team);
                participants.put(participantId, participant);
                team.participants.add(participant);

                // No need to adjust pointCounts here
                return participant;
            } else {
                return null;
            }
        } else if (obj instanceof Participant) {
            Participant participant = (Participant) obj;
            if (participant.id == participantId) {
                return participant;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    private static void updateParticipantPoints(Participant participant, int delta) {
        Team team = participant.team;
        int oldPoints = participant.points;
        int newPoints = participant.points + delta;

        // Update team point counts
        long oldCount = team.pointCounts.get(oldPoints) - 1;
        if (oldCount == 0) {
            team.pointCounts.remove(oldPoints);
        } else {
            team.pointCounts.put(oldPoints, oldCount);
        }

        participant.points = newPoints;
        team.totalPoints += delta;

        if (newPoints > 0) {
            team.pointCounts.put(newPoints, team.pointCounts.getOrDefault(newPoints, 0L) + 1);
        } else {
            // Remove participant if points reach zero or below
            team.participants.remove(participant);
            team.memberCount--;
            participants.remove(participant.id);
        }

        team.uniquePointCount = team.pointCounts.size();
    }

    private static void removeTopParticipants(Team team, TreeMap<Integer, Object> participantsMap, int count) {
        // Collect points and counts in descending order
        TreeMap<Integer, Long> pointCountsDesc = new TreeMap<>((a, b) -> b - a);
        pointCountsDesc.putAll(team.pointCounts);

        int removed = 0;
        for (Map.Entry<Integer, Long> entry : pointCountsDesc.entrySet()) {
            int points = entry.getKey();
            long numParticipants = entry.getValue();

            // Collect participants with this point value
            MyLinkedList<Participant> participantsList = new MyLinkedList<>();
            for (int i = 0; i < team.participants.size(); i++) {
                Object obj = team.participants.get(i);
                if (obj instanceof Participant) {
                    Participant p = (Participant) obj;
                    if (p.points == points) {
                        participantsList.add(p);
                    }
                } else if (obj instanceof ParticipantRange) {
                    ParticipantRange pr = (ParticipantRange) obj;
                    if (pr.points == points) {
                        // Split all participants in the range
                        for (int id = pr.startId; id <= pr.endId; id++) {
                            Participant p = new Participant(id, pr.points, team);
                            participantsList.add(p);
                        }
                    }
                }
            }

            // Sort participantsList
            sortParticipants(participantsList);

            for (int i = 0; i < participantsList.size() && removed < count; i++) {
                Participant p = participantsList.get(i);
                removeParticipantFromTeam(p, participantsMap);
                removed++;
            }

            if (removed >= count) {
                break;
            }
        }
    }

    private static void setAllParticipantsPointsToOne(Team team) {
        // Reset team point counts
        team.pointCounts.clear();
        team.uniquePointCount = 1; // Since all points will be 1

        team.totalPoints = team.memberCount * 1;
        team.pointCounts.put(1, team.memberCount);

        // Update all participants and ranges
        for (int i = 0; i < team.participants.size(); i++) {
            Object obj = team.participants.get(i);
            if (obj instanceof Participant) {
                Participant p = (Participant) obj;
                p.points = 1;
            } else if (obj instanceof ParticipantRange) {
                ParticipantRange pr = (ParticipantRange) obj;
                pr.points = 1;
            }
        }
    }

    private static void eliminateTeam(Team team, MyLinkedList<Team> teamList, TreeMap<Integer, Team> teamsMap,
            TreeMap<Integer, Object> participantsMap) {
        teamList.remove(team);
        teamsMap.remove(team.id);
        // Remove all participants from the participants map
        for (int i = 0; i < team.participants.size(); i++) {
            Object obj = team.participants.get(i);
            if (obj instanceof Participant) {
                Participant p = (Participant) obj;
                participantsMap.remove(p.id);
            } else if (obj instanceof ParticipantRange) {
                ParticipantRange pr = (ParticipantRange) obj;
                participantsMap.remove(pr.startId);
            }
        }
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

    private static Team getTeamWithHighestPoints(MyLinkedList<Team> teamList) {
        Team highestTeam = teamList.get(0);
        for (int i = 0; i < teamList.size(); i++) {
            Team team = teamList.get(i);
            if (team.totalPoints > highestTeam.totalPoints) {
                highestTeam = team;
            }
        }
        return highestTeam;
    }

    private static void removeParticipantFromTeam(Participant participant,
            TreeMap<Integer, Object> participantsMap) {
        Team team = participant.team;
        int points = participant.points;
        team.participants.remove(participant);
        team.memberCount--;
        team.totalPoints -= points;

        long count = team.pointCounts.get(points) - 1;
        if (count == 0) {
            team.pointCounts.remove(points);
        } else {
            team.pointCounts.put(points, count);
        }

        team.uniquePointCount = team.pointCounts.size();

        participantsMap.remove(participant.id);
    }

    private static void sortParticipants(MyLinkedList<Participant> participantList) {
        // Implement a simple insertion sort
        int n = participantList.size();
        for (int i = 1; i < n; ++i) {
            Participant key = participantList.get(i);
            int j = i - 1;

            while (j >= 0 && compareParticipants(participantList.get(j), key) > 0) {
                participantList.set(j + 1, participantList.get(j));
                j = j - 1;
            }
            participantList.set(j + 1, key);
        }
    }

    private static int compareParticipants(Participant a, Participant b) {
        if (a.points != b.points) {
            return b.points - a.points; // Descending points
        } else if (a.matches != b.matches) {
            return a.matches - b.matches; // Ascending matches
        } else {
            return a.id - b.id; // Ascending ID
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
            return Long.compare(b.totalPoints, a.totalPoints);
        } else if (a.memberCount != b.memberCount) {
            return Long.compare(a.memberCount, b.memberCount);
        } else {
            return a.id - b.id;
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
                    if (line == null)
                        return null;
                    tokenizer = new StringTokenizer(line);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            return tokenizer.nextToken();
        }

        public int nextInteger() {
            String token = next();
            if (token == null)
                return -1;
            return Integer.parseInt(token);
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
    }
}
