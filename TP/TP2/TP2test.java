import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

public class TP2test {
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

    static class Team {
        int id;
        List<Participant> participants;
        int totalPoints;
        int memberCount;
        int uniquePointCount;
        int[] pointCounts;
        int timesCaught;

        public Team(int id) {
            this.id = id;
            this.participants = new ArrayList<>();
            this.totalPoints = 0;
            this.memberCount = 0;
            this.uniquePointCount = 0;
            this.pointCounts = new int[1001];
            this.timesCaught = 0;
        }
    }

    public static void main(String[] args) throws IOException {
        InputStream inputStream = System.in;
        OutputStream outputStream = System.out;
        InputReader in = new InputReader(inputStream);
        out = new PrintWriter(outputStream);

        Map<Integer, Participant> participants = new HashMap<>();
        Map<Integer, Team> teams = new HashMap<>();
        List<Team> teamList = new ArrayList<>();
        Set<Integer> eliminatedParticipants = new HashSet<>();

        int participantIdCounter = 1;
        int teamIdCounter = 1;

        int M = in.nextInteger();
        int[] Mi = new int[M];
        for (int i = 0; i < M; i++) {
            Mi[i] = in.nextInteger();
        }

        Team jokiTeam = null;
        Team sofitaTeam = null;

        int index = 0;
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
                
                if (team.pointCounts[points] == 0) {
                    team.uniquePointCount++;
                }
                team.pointCounts[points]++;
            }

            teams.put(team.id, team);
            teamList.add(team);
            if (team.id == 1) {
                sofitaTeam = team;
            }
            teamIdCounter++;
        }

        int lowestPoints = Integer.MAX_VALUE;
        for (Team team : teamList) {
            if (team.totalPoints < lowestPoints) {
                lowestPoints = team.totalPoints;
                jokiTeam = team;
            }
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
                    long jumlahPesertaBaru = Long.parseLong(parts[1]);
                    for (int i = 0; i<jumlahPesertaBaru;i++){
                        Participant participant = new Participant(participantIdCounter, 3, sofitaTeam);
                        participants.put(participant.id, participant);
                        sofitaTeam.participants.add(participant);
                        sofitaTeam.totalPoints += 3;
                        sofitaTeam.memberCount++;
                
                        if (sofitaTeam.pointCounts[3] == 0) {
                            sofitaTeam.uniquePointCount++;
                        }
                        sofitaTeam.pointCounts[3]++;
                    }
                    out.println(sofitaTeam.memberCount);
                    break;
                case 'M':
                    String direction = parts[1];
                    int sofitaIndex = teamList.indexOf(sofitaTeam);
                    if (direction.equals("L")){
                        sofitaIndex = (sofitaIndex - 1 + teamList.size()) % teamList.size();
                    } else if (direction.equals("R")){
                        sofitaIndex = (sofitaIndex+1) % teamList.size();
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
                                sofitaTeam = teamList.get(0); // Assuming teamList is sorted by totalPoints descending
                            } else {
                                sofitaTeam = null;
                            }
                        }
                        // Joki is expelled and moves to team with lowest total points
                        jokiTeam = getTeamWithLowestPointsExcept(teamList, sofitaTeam);
                    }

                    if (sofitaTeam.memberCount < 7) {
                        eliminateTeam(sofitaTeam, teamList, teams, participants);
                        // Move Sofita to the team with highest total points
                        if (!teamList.isEmpty()) {
                            // We need to find the team with the highest total points
                            Team highestTeam = teamList.get(0);
                            for (Team team : teamList) {
                                if (team.totalPoints > highestTeam.totalPoints) {
                                    highestTeam = team;
                                }
                            }
                            sofitaTeam = highestTeam;
                        } else {
                            sofitaTeam = null;
                        }
                    }
                    
                    out.println(sofitaTeam != null ? sofitaTeam.id : -1);
                    break;                
                case 'J':
                    String jokiDirection = parts[1];
                    int jokiIndex = teamList.indexOf(jokiTeam);
                    if (jokiDirection.equals("L")){
                        jokiIndex = (jokiIndex - 1 + teamList.size()) % teamList.size();
                    } else if (jokiDirection.equals("R")){
                        jokiIndex = (jokiIndex+1) % teamList.size();
                    }
                    Team destinationTeam = teamList.get(jokiIndex);
                    if (destinationTeam.id != sofitaTeam.id){
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
                    // Add seven new participants with points = 1
                    for (int i = 0; i < 7; i++) {
                        Participant participant = new Participant(participantIdCounter++, 1, newTeam);
                        participants.put(participant.id, participant);
                        newTeam.participants.add(participant);
                        newTeam.totalPoints += 1;
                        newTeam.memberCount++;
                
                        if (newTeam.pointCounts[1] == 0) {
                            newTeam.uniquePointCount++;
                        }
                        newTeam.pointCounts[1]++;
                    }
                    // Insert the new team to the left or right of Sofita's current team
                    int sofitaIndexG = teamList.indexOf(sofitaTeam);
                    if (directionG.equals("L")) {
                        teamList.add(sofitaIndexG, newTeam);
                    } else if (directionG.equals("R")) {
                        teamList.add(sofitaIndexG + 1, newTeam);
                    }
                    teams.put(newTeam.id, newTeam);
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
                    List<Team> eliminatedTeams = new ArrayList<>();
                    for (int i = 0; i < teamList.size(); i++) {
                        Team team = teamList.get(i);
                        if (team.totalPoints < threshold) {
                            eliminatedTeams.add(team);
                            teamList.remove(i);
                            i--;
                            teams.remove(team.id);
                        }
                    }
                    out.println(eliminatedTeams.size());
                
                    // Update Sofita's team if necessary
                    if (eliminatedTeams.contains(sofitaTeam)) {
                        if (!teamList.isEmpty()) {
                            sofitaTeam = teamList.get(0);
                        } else {
                            sofitaTeam = null;
                        }
                    }
                    break;
                
                case 'R':
                    mergeSortTeams(teamList, 0, teamList.size() - 1);
                    if (!teamList.isEmpty()) {
                        sofitaTeam = teamList.get(0);
                        out.println(sofitaTeam.id);
                    } else {
                        out.println(-1);
                    }
                    break;

                case 'B':
                    String boundType = parts[1];
                
                    int K = sofitaTeam.participants.size();
                    int[] pointsArray = new int[K];
                    for (int i = 0; i < K; i++) {
                        pointsArray[i] = sofitaTeam.participants.get(i).points;
                    }
                
                    // Sort the pointsArray
                    mergeSort(pointsArray, 0, K - 1);
                
                    int indexQ1 = Math.max(0, (int) Math.floor(0.25 * K) - 1);
                    int indexQ3 = Math.min(K - 1, (int) Math.floor(0.75 * K) - 1);
                
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
            team.participants.remove(participant);
            team.memberCount--;
        }
    }    

    public static void mergeSortTeams(List<Team> list, int left, int right) {
        if (left < right) {
            int mid = (left + right) / 2;
            mergeSortTeams(list, left, mid);
            mergeSortTeams(list, mid + 1, right);
            mergeTeams(list, left, mid, right);
        }
    }
    
    public static void mergeTeams(List<Team> list, int left, int mid, int right) {
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

    private static void removeTopParticipants(Team team, Map<Integer, Participant> participantsMap, int count) {
        // Sort participants based on points descending, matches ascending, id ascending
        List<Participant> sortedParticipants = new ArrayList<>(team.participants);
        sortParticipants(sortedParticipants);
        int removed = 0;
        for (Participant p : sortedParticipants) {
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
        for (Participant p : team.participants) {
            p.points = 1;
        }
        team.pointCounts[1] = team.memberCount;
    }
    
    private static void eliminateTeam(Team team, List<Team> teamList, Map<Integer, Team> teamsMap, Map<Integer, Participant> participantsMap) {
        teamList.remove(team);
        teamsMap.remove(team.id);
        // Remove all participants from the participants map
        for (Participant p : team.participants) {
            participantsMap.remove(p.id);
        }
    }
    
    private static Team getTeamWithLowestPointsExcept(List<Team> teamList, Team excludeTeam) {
        Team lowestTeam = null;
        for (Team team : teamList) {
            if (team == excludeTeam) continue;
            if (lowestTeam == null || team.totalPoints < lowestTeam.totalPoints) {
                lowestTeam = team;
            }
        }
        return lowestTeam;
    }
    
    private static void removeParticipantFromTeam(Participant participant, Map<Integer, Participant> participantsMap) {
        Team team = participant.team;
        int points = participant.points;
        team.participants.remove(participant);
        team.memberCount--;
        team.totalPoints -= points;
        team.pointCounts[points]--;
        if (team.pointCounts[points] == 0) {
            team.uniquePointCount--;
        }
        participantsMap.remove(participant.id);
    }

    private static void sortParticipants(List<Participant> participantList) {
        // Implement a sorting algorithm, for example, insertion sort
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
