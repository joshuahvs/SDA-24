import java.io.*;
import java.util.*;

public class TP2GPT1 {
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
        int[] pointCounts; // For points from 0 to 1000

        public Team(int id) {
            this.id = id;
            this.participants = new ArrayList<>();
            this.totalPoints = 0;
            this.memberCount = 0;
            this.uniquePointCount = 0;
            this.pointCounts = new int[1001]; // Points range from 0 to 1000
        }
    }

    public static void main(String[] args) throws IOException {
        new TP2GPT1().run();
    }

    // Map to store participants by their ID
    Map<Integer, Participant> participants = new HashMap<>();
    // Map to store teams by their ID
    Map<Integer, Team> teams = new HashMap<>();
    // List to maintain the order of teams
    List<Team> teamList = new ArrayList<>();
    // Set to keep track of eliminated participants
    Set<Integer> eliminatedParticipants = new HashSet<>();

    int participantIdCounter = 1;
    int teamIdCounter = 1;

    // Cheater and Sofita's current team
    Team cheaterTeam = null;
    Team sofitaTeam = null;

    void run() throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        // Read initial data
        int M = Integer.parseInt(br.readLine());
        String[] MiStr = br.readLine().split(" ");
        String[] PjStr = br.readLine().split(" ");
        int index = 0;

        for (int i = 0; i < M; i++) {
            int Mi = Integer.parseInt(MiStr[i]);
            Team team = new Team(teamIdCounter++);
            for (int j = 0; j < Mi; j++) {
                int points = Integer.parseInt(PjStr[index++]);
                Participant participant = new Participant(participantIdCounter++, points, team);
                participants.put(participant.id, participant);
                team.participants.add(participant);
                team.totalPoints += points;
                team.memberCount++;

                // Update point counts
                if (team.pointCounts[points] == 0) {
                    team.uniquePointCount++;
                }
                team.pointCounts[points]++;
            }
            teams.put(team.id, team);
            teamList.add(team);
        }

        // Initially, Sofita supervises the first team
        sofitaTeam = teamList.get(0);

        // Initially, the cheater is in the team with the lowest total points
        cheaterTeam = teamList.get(0);
        for (Team team : teamList) {
            if (team.totalPoints < cheaterTeam.totalPoints) {
                cheaterTeam = team;
            }
        }

        int Q = Integer.parseInt(br.readLine());
        // Read and process commands
        for (int q = 0; q < Q; q++) {
            String cmdLine = br.readLine().trim();
            if (cmdLine.isEmpty()) {
                cmdLine = br.readLine().trim();
            }
            String[] cmdParts = cmdLine.split(" ");
            String cmd = cmdParts[0];

            switch (cmd) {
                case "A":
                    // Add participants to the team being supervised by Sofita
                    int jumlahPeserta = Integer.parseInt(cmdParts[1]);
                    Team currentTeam = sofitaTeam;
                    for (int i = 0; i < jumlahPeserta; i++) {
                        Participant participant = new Participant(participantIdCounter++, 3, currentTeam);
                        participants.put(participant.id, participant);
                        currentTeam.participants.add(participant);
                        currentTeam.totalPoints += 3;
                        currentTeam.memberCount++;

                        // Update point counts
                        if (currentTeam.pointCounts[3] == 0) {
                            currentTeam.uniquePointCount++;
                        }
                        currentTeam.pointCounts[3]++;
                    }
                    System.out.println(currentTeam.memberCount);
                    break;

                case "M":
                    // Move Sofita to the left or right team
                    String direction = cmdParts[1];
                    int idx = teamList.indexOf(sofitaTeam);
                    if (direction.equals("L")) {
                        idx = (idx - 1 + teamList.size()) % teamList.size();
                    } else {
                        idx = (idx + 1) % teamList.size();
                    }
                    sofitaTeam = teamList.get(idx);
                    System.out.println(sofitaTeam.id);
                    break;

                case "J":
                    // Move cheater to the left or right team
                    direction = cmdParts[1];
                    idx = teamList.indexOf(cheaterTeam);
                    if (direction.equals("L")) {
                        idx = (idx - 1 + teamList.size()) % teamList.size();
                    } else {
                        idx = (idx + 1) % teamList.size();
                    }
                    Team destinationTeam = teamList.get(idx);
                    if (destinationTeam == sofitaTeam) {
                        // Cheater stays in current team
                    } else {
                        cheaterTeam = destinationTeam;
                    }
                    System.out.println(cheaterTeam.id);
                    break;

                case "U":
                    // Count unique points in the team supervised by Sofita
                    System.out.println(sofitaTeam.uniquePointCount);
                    break;

                case "G":
                    // Generate a new team
                    direction = cmdParts[1];
                    Team newTeam = new Team(teamIdCounter++);
                    for (int i = 0; i < 7; i++) {
                        Participant participant = new Participant(participantIdCounter++, 1, newTeam);
                        participants.put(participant.id, participant);
                        newTeam.participants.add(participant);
                        newTeam.totalPoints += 1;
                        newTeam.memberCount++;

                        // Update point counts
                        if (newTeam.pointCounts[1] == 0) {
                            newTeam.uniquePointCount++;
                        }
                        newTeam.pointCounts[1]++;
                    }
                    idx = teamList.indexOf(sofitaTeam);
                    if (direction.equals("L")) {
                        teamList.add(idx, newTeam);
                    } else {
                        teamList.add(idx + 1, newTeam);
                    }
                    teams.put(newTeam.id, newTeam);
                    System.out.println(newTeam.id);
                    break;

                case "E":
                    // Eliminate teams with total points less than given value
                    int POIN = Integer.parseInt(cmdParts[1]);
                    int eliminatedTeamsCount = 0;
                    Iterator<Team> teamIterator = teamList.iterator();
                    while (teamIterator.hasNext()) {
                        Team team = teamIterator.next();
                        if (team.totalPoints < POIN) {
                            teamIterator.remove();
                            eliminatedTeamsCount++;
                            // Remove participants from the participants map
                            for (Participant p : team.participants) {
                                eliminatedParticipants.add(p.id);
                                participants.remove(p.id);
                            }
                        }
                    }
                    // Update Sofita's team if her team is eliminated
                    if (!teamList.contains(sofitaTeam)) {
                        if (!teamList.isEmpty()) {
                            // She supervises the team with the highest total points
                            sofitaTeam = teamList.get(0);
                            for (Team team : teamList) {
                                if (team.totalPoints > sofitaTeam.totalPoints) {
                                    sofitaTeam = team;
                                }
                            }
                        } else {
                            sofitaTeam = null;
                        }
                    }
                    System.out.println(eliminatedTeamsCount);
                    break;

                case "R":
                    // Reorder teams based on total points
                    teamList.sort((a, b) -> {
                        if (b.totalPoints != a.totalPoints) {
                            return b.totalPoints - a.totalPoints;
                        } else if (a.memberCount != b.memberCount) {
                            return a.memberCount - b.memberCount;
                        } else {
                            return a.id - b.id;
                        }
                    });
                    // Update Sofita's position
                    sofitaTeam = teamList.get(0);
                    // Cheater remains in the team he is in
                    System.out.println(sofitaTeam.id);
                    break;

                case "V":
                    // Participants compete
                    int ID_PESERTA1 = Integer.parseInt(cmdParts[1]);
                    int ID_PESERTA2 = Integer.parseInt(cmdParts[2]);
                    int ID_TIM = Integer.parseInt(cmdParts[3]);
                    int HASIL = Integer.parseInt(cmdParts[4]);
                    if (!participants.containsKey(ID_PESERTA1) || !participants.containsKey(ID_PESERTA2)
                            || eliminatedParticipants.contains(ID_PESERTA1) || eliminatedParticipants.contains(ID_PESERTA2)) {
                        System.out.println(-1);
                        break;
                    }
                    Participant p1 = participants.get(ID_PESERTA1);
                    Participant p2 = participants.get(ID_PESERTA2);
                    if (p1.team != sofitaTeam || p2.team.id != ID_TIM) {
                        System.out.println(-1);
                        break;
                    }
                    if (HASIL == 0) {
                        p1.points += 1;
                        p2.points += 1;
                        p1.team.totalPoints += 1;
                        p2.team.totalPoints += 1;

                        // Update point counts for p1
                        updatePointCounts(p1.team, p1.points - 1, p1.points);

                        // Update point counts for p2
                        updatePointCounts(p2.team, p2.points - 1, p2.points);

                        System.out.println(p1.points + " " + p2.points);
                    } else if (HASIL == 1) {
                        p1.points += 3;
                        p2.points -= 3;
                        p1.team.totalPoints += 3;
                        p2.team.totalPoints -= 3;

                        // Update point counts for p1
                        updatePointCounts(p1.team, p1.points - 3, p1.points);

                        // Update point counts for p2
                        updatePointCounts(p2.team, p2.points + 3, p2.points);

                        // Check for elimination
                        if (p2.points <= 0) {
                            eliminatedParticipants.add(p2.id);
                            p2.team.memberCount--;
                            // Remove from point counts
                            p2.team.pointCounts[p2.points]--;
                            if (p2.team.pointCounts[p2.points] == 0) {
                                p2.team.uniquePointCount--;
                            }
                        }
                        System.out.println(p1.points);
                    } else if (HASIL == -1) {
                        p1.points -= 3;
                        p2.points += 3;
                        p1.team.totalPoints -= 3;
                        p2.team.totalPoints += 3;

                        // Update point counts for p1
                        updatePointCounts(p1.team, p1.points + 3, p1.points);

                        // Update point counts for p2
                        updatePointCounts(p2.team, p2.points - 3, p2.points);

                        // Check for elimination
                        if (p1.points <= 0) {
                            eliminatedParticipants.add(p1.id);
                            p1.team.memberCount--;
                            // Remove from point counts
                            p1.team.pointCounts[p1.points]--;
                            if (p1.team.pointCounts[p1.points] == 0) {
                                p1.team.uniquePointCount--;
                            }
                        }
                        System.out.println(p2.points);
                    }
                    break;

                case "T":
                    // Participant transfers points to another participant
                    ID_PESERTA1 = Integer.parseInt(cmdParts[1]);
                    ID_PESERTA2 = Integer.parseInt(cmdParts[2]);
                    int JUMLAH_POIN = Integer.parseInt(cmdParts[3]);
                    if (!participants.containsKey(ID_PESERTA1) || !participants.containsKey(ID_PESERTA2)
                            || eliminatedParticipants.contains(ID_PESERTA1) || eliminatedParticipants.contains(ID_PESERTA2)) {
                        System.out.println(-1);
                        break;
                    }
                    p1 = participants.get(ID_PESERTA1);
                    p2 = participants.get(ID_PESERTA2);
                    if (p1.team != sofitaTeam || p2.team != sofitaTeam) {
                        System.out.println(-1);
                        break;
                    }
                    if (JUMLAH_POIN >= p1.points) {
                        System.out.println(-1);
                        break;
                    }
                    p1.points -= JUMLAH_POIN;
                    p2.points += JUMLAH_POIN;

                    // Update point counts for p1
                    updatePointCounts(p1.team, p1.points + JUMLAH_POIN, p1.points);

                    // Update point counts for p2
                    updatePointCounts(p2.team, p2.points - JUMLAH_POIN, p2.points);

                    System.out.println(p1.points + " " + p2.points);
                    break;

                case "B":
                    // Outlier calculation in the current team
                    String EXTREME_BOUND = cmdParts[1];
                    int K = sofitaTeam.memberCount;
                    if (K == 0) {
                        System.out.println(0);
                        break;
                    }
                    int[] counts = sofitaTeam.pointCounts.clone();
                    int[] prefixSums = new int[1001];
                    prefixSums[0] = counts[0];
                    for (int i = 1; i <= 1000; i++) {
                        prefixSums[i] = prefixSums[i - 1] + counts[i];
                    }

                    // Find Q1 and Q3
                    int Q1Index = Math.max(0, (int) Math.floor(0.25 * K - 1));
                    int Q3Index = Math.min(K - 1, (int) Math.floor(0.75 * K - 1));

                    int Q1 = findPointAtIndex(prefixSums, Q1Index);
                    int Q3 = findPointAtIndex(prefixSums, Q3Index);
                    int IQR = Q3 - Q1;
                    int L = Q1 - (int) Math.floor(1.5 * IQR);
                    int U = Q3 + (int) Math.floor(1.5 * IQR);

                    int count = 0;
                    if (EXTREME_BOUND.equals("U")) {
                        if (U < 0) {
                            count = K;
                        } else if (U >= 1000) {
                            count = 0;
                        } else {
                            count = prefixSums[1000] - prefixSums[U];
                        }
                    } else {
                        if (L < 0) {
                            count = 0;
                        } else if (L >= 1000) {
                            count = K;
                        } else {
                            count = prefixSums[L];
                        }
                    }
                    System.out.println(count);
                    break;
            }
        }
    }

    void updatePointCounts(Team team, int oldPoints, int newPoints) {
        // Decrease count for oldPoints
        team.pointCounts[oldPoints]--;
        if (team.pointCounts[oldPoints] == 0) {
            team.uniquePointCount--;
        }
        // Increase count for newPoints
        if (team.pointCounts[newPoints] == 0) {
            team.uniquePointCount++;
        }
        team.pointCounts[newPoints]++;
    }

    int findPointAtIndex(int[] prefixSums, int index) {
        for (int i = 0; i <= 1000; i++) {
            if (prefixSums[i] > index) {
                return i;
            }
        }
        return 1000;
    }
}
