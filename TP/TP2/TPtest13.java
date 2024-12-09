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
import java.util.Set;
import java.util.StringTokenizer;

public class TPtest13 {
    private static PrintWriter out;

    static class Participant implements Comparable<Participant> {
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
        public int compareTo(Participant other) {
            // Membandingkan berdasarkan points, jika sama maka bandingkan berdasarkan matches, jika sama bandingkan berdasarkan id
            if (this.points != other.points) {
                return Integer.compare(other.points, this.points); // descending order by points
            }
            if (this.matches != other.matches) {
                return Integer.compare(this.matches, other.matches); // ascending order by matches
            }
            return Integer.compare(this.id, other.id); // ascending order by id
        }
    }
    

    static class Team implements Comparable<Team> {
        int id;
        MyLinkedList<Participant> participants;  // Menyimpan semua participant dari team ini
        int totalPoints;  // Total points dari tim
        int memberCount;  // Jumlah member dalam tim
        MyTree<Integer> pointCounts;  // Menggunakan MyTree untuk menyimpan point counts per tim
        int timesCaught;  // Berapa kali tim ini tertangkap
        MyTree<Participant> participantTree;  // Menggunakan AVL tree untuk peserta
    
        public Team(int id) {
            this.id = id;
            this.participants = new MyLinkedList<>();
            this.totalPoints = 0;
            this.memberCount = 0;
            // this.uniquePointCount = 0;
            this.pointCounts = new MyTree<>();
            this.timesCaught = 0;
            this.participantTree = new MyTree<>();
        }
    
        @Override
        public int compareTo(Team other) {
            // Membandingkan berdasarkan totalPoints, jika sama bandingkan berdasarkan memberCount, jika sama bandingkan berdasarkan id
            if (this.totalPoints != other.totalPoints) {
                return Integer.compare(other.totalPoints, this.totalPoints); // descending order by total points
            }
            if (this.memberCount != other.memberCount) {
                return Integer.compare(this.memberCount, other.memberCount); // ascending order by member count
            }
            return Integer.compare(this.id, other.id); // ascending order by id
        }
    }
    
    
    static MyTree<Participant> participants = new MyTree<>();
    static MyTree<Team> teams = new MyTree<>();
    static MyLinkedList<Team> teamList = new MyLinkedList<>();
    static HashMap<Integer, Participant> participantsMap = new HashMap<>();

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
                participants.insert(participant); // Menggunakan insert untuk memasukkan peserta ke dalam AVL Tree
                participantsMap.put(participant.id, participant);
                team.participants.add(participant);
                team.totalPoints += points;
                team.memberCount++;
        
                // // Mengelola unique points (poin unik)
                // if (!team.pointCounts.contains(points)) {
                //     team.uniquePointCount++; // Jika poin belum ada dalam set poin, tambahkan jumlah poin unik
                // }
                team.pointCounts.insert(points); // Memasukkan poin ke dalam AVL Tree
        
                // Tambahkan participant ke participantTree
                team.participantTree.insert(participant); // Menggunakan insert untuk AVL Tree
            }
        
            // Menambahkan tim ke dalam teams dan teamList
            teams.insert(team); // Menggunakan insert untuk memasukkan tim ke dalam AVL Tree
            teamList.add(team);
            if (team.id == 1) {
                sofitaTeam = team; // Mengatur tim Sofita
            }
            teamIdCounter++;
        }
        

        // // Mencari tim dengan poin paling rendah (tempat joki)
        // jokiTeam = getTeamWithLowestPointsExcept(teamList, sofitaTeam);

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

            if (command == 'A'){
                // printTeamList();
                // out.println("sofita team id sebelum A = " + sofitaTeam.id);
                // Parse the number of new participants
                long jumlahPesertaBaru = Long.parseLong(parts[1]);
                if (sofitaTeam != null) {
                    for (int i = 0; i < jumlahPesertaBaru; i++) {
                        Participant participant = new Participant(participantIdCounter++, 3, sofitaTeam);
                        participants.insert(participant); // Menggunakan insert untuk menambahkan peserta ke AVL Tree participants
                        participantsMap.put(participant.id, participant);
                        sofitaTeam.participants.add(participant);
                        sofitaTeam.totalPoints += 3;
                        sofitaTeam.memberCount++;

                        // Mengelola unique points (poin unik) menggunakan pointCounts
                        // if (!sofitaTeam.pointCounts.contains(3)) {
                        //     sofitaTeam.uniquePointCount++; // Jika poin 3 belum ada dalam AVL Tree pointCounts, tambahkan uniquePointCount
                        // }
                        sofitaTeam.pointCounts.insert(3); // Masukkan poin 3 ke AVL Tree pointCounts

                        // Tambahkan participant ke participantTree
                        sofitaTeam.participantTree.insert(participant); // Menggunakan insert untuk AVL Tree
                    }

                    // Cetak jumlah anggota yang telah diperbarui
                    out.println(sofitaTeam.memberCount);
                } else {
                    out.println(-1);
                }
                // out.println("sofita team id sesudah A = " + sofitaTeam.id);
            }
            else if (command == 'B'){
                String boundType = parts[1];

                if (sofitaTeam!= null){
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
                }else{
                    out.println(-1);
                }
            }
            else if (command == 'M'){
                // printTeamList();
                // if (jokiTeam!= null && sofitaTeam!= null){
                //     out.println("joki team sebelum M = " + jokiTeam.id);
                //     out.println("sofita team sebelum M = "+ sofitaTeam.id);
                // } else{
                //     out.println("ada NULL");
                // }
                String direction = parts[1];
                int sofitaIndex = teamList.indexOf(sofitaTeam);
                if (teamList.size() > 0){
                    if (direction.equals("L")) {
                        sofitaIndex = (sofitaIndex - 1 + teamList.size()) % teamList.size();
                    } else if (direction.equals("R")) {
                        sofitaIndex = (sofitaIndex + 1) % teamList.size();
                    }
                    sofitaTeam = teamList.get(sofitaIndex);
                } else{
                    continue;
                }
                do{
                    // Check if Joki is in the same team
                    if (jokiTeam != null && jokiTeam!= null && sofitaTeam.id == jokiTeam.id) {
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
                            // out.println("remove disini 1");
                            eliminateTeam(sofitaTeam, teamList, teams, participants);
                            // Move Sofita to team with highest total points
                            if (!teamList.isEmpty()) {
                                sofitaTeam = getTeamWithHighestPoints(teamList);
                            } else {
                                sofitaTeam = null;
                            }
                        }
                        // Joki is expelled and moves to team with lowest total points
                        jokiTeam = getTeamWithLowestPoints(teamList, sofitaTeam);
                        // out.println("joki team id " + jokiTeam.id);
                    }

                    if (sofitaTeam != null && sofitaTeam.memberCount < 7) {
                        // out.println("remove disini 2");
                        // out.println("sofita team member count " + sofitaTeam.memberCount);
                        eliminateTeam(sofitaTeam, teamList, teams, participants);
                        // Move Sofita to the team with highest total points
                        if (!teamList.isEmpty()) {
                            sofitaTeam = getTeamWithHighestPoints(teamList);
                        } else {
                            sofitaTeam = null;
                        }
                    }
                } while (sofitaTeam!= null && jokiTeam!= null && sofitaTeam.id == jokiTeam.id);

                if (sofitaTeam!=null){
                    out.println(sofitaTeam.id);
                    // out.println("sofitateam sesudah M = "+ sofitaTeam.id);
                } else{
                    out.println(-1);
                    // out.println("ada NULL");
                }

                // if (jokiTeam!= null){
                //     out.println("joki team sesudah M = " + jokiTeam.id);
                // } else{
                //     out.println("ada NULL");
                // }
            }
            else if (command == 'T'){
                // out.println("sofita team id sebelum T= " + sofitaTeam.id);
                // printTeamList();
                // if (jokiTeam!= null && sofitaTeam!= null){
                //     out.println("joki team sebelum T = " + jokiTeam.id);
                //     out.println("sofita team sebelum T = "+ sofitaTeam.id);
                // } else{
                //     out.println("ada NULL");
                // }
                int senderId = Integer.parseInt(parts[1]);
                int receiverId = Integer.parseInt(parts[2]);
                int amount = Integer.parseInt(parts[3]);

                Participant sender = getParticipantById(senderId);
                Participant receiver = getParticipantById(receiverId);

                if (sender == null || receiver == null) {
                    out.println(-1);
                } else if (sender.team != sofitaTeam || receiver.team != sofitaTeam){
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
                        // out.println("remove disini 4");
                        eliminateTeam(sender.team, teamList, teams, participants);

                        // Move Sofita to the team with highest total points
                        if (!teamList.isEmpty()) {
                            sofitaTeam = getTeamWithHighestPoints(teamList);
                        } else {
                            sofitaTeam = null;
                        }
                    }
                    out.println(sender.points + " " + receiver.points);
                    // out.println("sofita team id sesudah T = " + sofitaTeam.id);
                }

                // if (jokiTeam!= null && sofitaTeam!= null){
                //     out.println("joki team sesudah T = " + jokiTeam.id);
                //     out.println("sofita team sesudah T = "+ sofitaTeam.id);
                // } else{
                //     out.println("ada NULL");
                // }
            }
            else if (command == 'G'){
                // out.println("sofita team id sebelum G = " + sofitaTeam.id);
                String directionG = parts[1];
                Team newTeam = new Team(teamIdCounter);

                for (int i = 0; i < 7; i++) {
                    int points = 1; // Semua peserta memiliki poin = 1
                    Participant participant = new Participant(participantIdCounter++, points, newTeam);
                    participants.insert(participant); // Menggunakan insert untuk menambahkan peserta ke AVL Tree participants
                    participantsMap.put(participant.id, participant);
                    newTeam.participants.add(participant);
                    newTeam.totalPoints += points; // Tambahkan 1 poin ke totalPoints
                    newTeam.memberCount++;

                    // Update uniquePointCount dan pointCounts dengan benar
                    // if (!newTeam.pointCounts.contains(points)) {
                    //     newTeam.uniquePointCount++; // Jika poin belum ada dalam AVL Tree, tambahkan uniquePointCount
                    // }
                    newTeam.pointCounts.insert(points); // Masukkan poin ke AVL Tree

                    // Tambahkan participant ke participantTree
                    newTeam.participantTree.insert(participant); // Menggunakan insert untuk AVL Tree
                }

                int sofitaIndex = teamList.indexOf(sofitaTeam);
                if (directionG.equals("L")) {
                    teamList.add(sofitaIndex, newTeam);
                } else if (directionG.equals("R")) {
                    teamList.add(sofitaIndex + 1, newTeam);
                }

                teams.insert(newTeam); // Menggunakan insert untuk menambahkan tim ke AVL Tree teams
                teamIdCounter++;
                out.println(newTeam.id);
            }
            else if (command == 'V'){
                // if (jokiTeam!= null && sofitaTeam!= null){
                //     out.println("joki team sebelum V = " + jokiTeam.id);
                //     out.println("sofita team sebelum V = "+ sofitaTeam.id);
                // } else{
                //     out.println("ada NULL");
                // }
                // out.println("sofita team id = " + sofitaTeam.id);
                int participant1Id = Integer.parseInt(parts[1]);
                int participant2Id = Integer.parseInt(parts[2]);
                int opponentTeamId = Integer.parseInt(parts[3]);
                int result = Integer.parseInt(parts[4]);

                Participant participant1 = getParticipantById(participant1Id);
                Team opponentTeam = getTeamById(opponentTeamId); // Menggunakan metode getTeamById untuk mencari tim berdasarkan ID
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

                        // Check if participant's team needs to be eliminated
                        if (participant2.team.memberCount < 7) {
                            eliminateTeam(participant2.team, teamList, teams, participants);

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

                        // Check if participant's team needs to be eliminated
                        if (participant1.team.memberCount < 7) {
                            eliminateTeam(participant1.team, teamList, teams, participants);

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

                // if (jokiTeam!= null && sofitaTeam!= null){
                //     out.println("joki team sesudah V = " + jokiTeam.id);
                //     out.println("sofita team sesudah V = "+ sofitaTeam.id);
                // } else{
                //     out.println("ada NULL");
                // }
                // out.println("participant team id = " + participant2.team.id);

            }
            else if (command == 'E'){
                // printTeamList();
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
                    // out.println("remove disini 3");
                    eliminateTeam(team, teamList, teams, participants);
            
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
            }            
            else if (command == 'U'){
                // if (sofitaTeam != null) {
                //     out.println("Debugging Unique Points for Sofita Team:");
                //     printParticipant(sofitaTeam);  // Debugging output
                //     out.println("Unique Point Count: " + sofitaTeam.uniquePointCount);
                // } else {
                //     out.println(-1);
                // }
                handleUniquePointsQuery(sofitaTeam);
            }
            else if (command == 'R'){
                mergeSortTeams(teamList, 0, teamList.size() - 1);
                if (!teamList.isEmpty()) {
                    sofitaTeam = teamList.get(0);
                    
                    // out.println();
                    // out.println("cetak id sofita = " + sofitaTeam.id);
                    // out.println("cetak id joki = " + jokiTeam.id);
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
                            // out.println("remove disini 5");
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
                        jokiTeam = getTeamWithLowestPoints(teamList, sofitaTeam);
                    }
                    
                    out.println(sofitaTeam != null ? sofitaTeam.id : -1);
                } else {
                    out.println(-1);
                }
                // printTeamList();
                // out.println("sofita team = " + sofitaTeam.id);
                // out.println("joki team = " + jokiTeam.id);
                // out.println();
            }
            else if (command == 'J'){
                String jokiDirection = parts[1];
                int jokiIndex = teamList.indexOf(jokiTeam);
                // printTeamList();
                // if (jokiTeam!= null && sofitaTeam!= null){
                //     out.println("joki team sebelum J = " + jokiTeam.id);
                //     out.println("sofita team sebelum J = "+ sofitaTeam.id);
                // } else{
                //     out.println("ada NULL");
                // }
                if (teamList.size()!=0){
                    if (jokiDirection.equals("L")) {
                        jokiIndex = (jokiIndex - 1 + teamList.size()) % teamList.size();
                    } else if (jokiDirection.equals("R")) {
                        jokiIndex = (jokiIndex + 1) % teamList.size();
                    }
                    Team destinationTeam = teamList.get(jokiIndex);
                    if (destinationTeam.id != sofitaTeam.id) {
                        jokiTeam = teamList.get(jokiIndex);
                    }
                    if (jokiTeam != null){
                        out.println(jokiTeam.id);
                    } else{
                        out.println(-1);
                    }
                } else{
                    out.println(-1);
                }
                // if (jokiTeam!= null && sofitaTeam!= null){
                //     out.println("joki team sesudah J = " + jokiTeam.id);
                //     out.println("sofita team sesudah J = "+ sofitaTeam.id);
                // } else{
                //     out.println("ada NULL");
                // }
            }
        }
        out.close();
    }

    // private static void printParticipant(Team team) {
    //     if (team == null) {
    //         out.println("Team is null. No participants to display.");
    //         return;
    //     }
    
    //     out.println("---- Participants in Team ID: " + team.id + " ----");
    //     out.println("Total Points: " + team.totalPoints);
    //     out.println("Member Count: " + team.memberCount);
    //     out.println("Unique Point Count: " + team.uniquePointCount);
    
    //     // Traverse through the linked list of participants and print their details
    //     MyLinkedList.Node<Participant> currentNode = team.participants.head;
    //     while (currentNode != null) {
    //         Participant p = currentNode.data;
    //         out.println("Participant ID: " + p.id + ", Points: " + p.points);
    //         currentNode = currentNode.next;
    //     }
    
    //     // Also print unique points from pointCounts tree for debugging purposes
    //     out.println("---- Unique Points in PointCounts Tree ----");
    //     printUniquePoints(team.pointCounts.root);
    
    //     out.println("-------------------------------------------");
    // }
    
    // Helper method to print unique points from the pointCounts AVL Tree
    private static void printUniquePoints(MyTree.TreeNode<Integer> node) {
        if (node == null) {
            return;
        }
        // Traverse left, root, and right to get sorted unique points
        printUniquePoints(node.left);
        out.println("Point: " + node.value);
        printUniquePoints(node.right);
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
    

    private static void removeTopParticipants(Team team, MyTree<Participant> participants, int numParticipants) {
        // Lakukan traversal inorder terbalik pada participantTree untuk mengumpulkan semua peserta
        List<Participant> allParticipants = new ArrayList<>();
        collectParticipantsInOrder(team.participantTree.root, allParticipants);
        
        // Sort daftar peserta berdasarkan poin dalam urutan menurun (descending)
        mergeSortParticipantsByPointsDescending(allParticipants, 0, allParticipants.size() - 1);
    
        // Ambil numParticipants peserta dengan poin tertinggi
        List<Participant> topNodes = new ArrayList<>();
        for (int i = 0; i < numParticipants && i < allParticipants.size(); i++) {
            topNodes.add(allParticipants.get(i));
        }
    
        // Hapus peserta dengan poin tertinggi dari tim dan update stats
        for (Participant p : topNodes) {
            // Remove participant from participantTree
            team.participantTree.delete(p);
    
            // Remove participant from team.participants (the linked list)
            team.participants.remove(p);
            // out.println("participant to be removed points = " + p.points);
    
            // Update team stats
            team.totalPoints -= p.points;
            team.memberCount--;
    
            // // Update pointCounts and uniquePointCount
            if (team.pointCounts.contains(p.points)) {
                // Assume each point is unique now since we do not count frequencies
                team.pointCounts.delete(p.points);
                // team.uniquePointCount--;
            }
            
            // Remove participant from the global participants AVL Tree
            participants.delete(p);
        }
    }

    private static void handleUniquePointsQuery(Team team) {
        if (team == null) {
            out.println(-1);
            return;
        }

        // Gunakan Set untuk mengumpulkan poin unik
        Set<Integer> uniquePoints = new HashSet<>();

        // Traverse melalui linked list participants untuk mengumpulkan poin
        MyLinkedList.Node<Participant> currentNode = team.participants.head;
        while (currentNode != null) {
            Participant p = currentNode.data;
            uniquePoints.add(p.points);
            currentNode = currentNode.next;
        }

        // Print ukuran dari set untuk menghitung jumlah poin unik
        out.println(uniquePoints.size());
    }

    
    // Fungsi untuk mengumpulkan peserta dari AVL Tree menggunakan inorder traversal (terbalik)
    private static void collectParticipantsInOrder(MyTree.TreeNode<Participant> node, List<Participant> participantsList) {
        if (node == null) {
            return;
        }
        // Traversal inorder terbalik untuk mendapatkan peserta dari yang tertinggi ke terendah
        collectParticipantsInOrder(node.right, participantsList);
        participantsList.add(node.value);
        collectParticipantsInOrder(node.left, participantsList);
    }
    
    // Fungsi Merge Sort untuk mengurutkan peserta berdasarkan poin (descending)
    private static void mergeSortParticipantsByPointsDescending(List<Participant> participants, int left, int right) {
        if (left < right) {
            int mid = (left + right) / 2;
            mergeSortParticipantsByPointsDescending(participants, left, mid);
            mergeSortParticipantsByPointsDescending(participants, mid + 1, right);
            mergeParticipants(participants, left, mid, right);
        }
    }
    
    // Fungsi merge untuk Merge Sort
    private static void mergeParticipants(List<Participant> participants, int left, int mid, int right) {
        int n1 = mid - left + 1;
        int n2 = right - mid;
    
        List<Participant> leftArray = new ArrayList<>();
        List<Participant> rightArray = new ArrayList<>();
    
        for (int i = 0; i < n1; ++i) {
            leftArray.add(participants.get(left + i));
        }
        for (int j = 0; j < n2; ++j) {
            rightArray.add(participants.get(mid + 1 + j));
        }
    
        int i = 0, j = 0, k = left;
        while (i < n1 && j < n2) {
            if (leftArray.get(i).points > rightArray.get(j).points) {
                participants.set(k, leftArray.get(i));
                i++;
            } else {
                participants.set(k, rightArray.get(j));
                j++;
            }
            k++;
        }
    
        while (i < n1) {
            participants.set(k, leftArray.get(i));
            i++;
            k++;
        }
    
        while (j < n2) {
            participants.set(k, rightArray.get(j));
            j++;
            k++;
        }
    }
    
    
    
    private static void setAllParticipantsPointsToOne(Team team) {
        team.totalPoints = team.memberCount; // Since all points will be 1
        team.pointCounts = new MyTree<>(); // Menggunakan AVL Tree baru untuk menyimpan poin
        // team.uniquePointCount = 1; // Sekarang hanya ada satu nilai poin unik
        team.pointCounts.insert(1); // Masukkan nilai 1 ke dalam pointCounts
    
        // Bersihkan participantTree dan bangun ulang
        team.participantTree = new MyTree<>(); // Membuat AVL Tree baru untuk participantTree
    
        MyLinkedList.Node<Participant> currentNode = team.participants.head;
        while (currentNode != null) {
            Participant p = currentNode.data;
            p.points = 1;
            // Insert into participantTree
            team.participantTree.insert(p);
            currentNode = currentNode.next;
        }
    }
    

    private static void eliminateTeam(Team team, MyLinkedList<Team> teamList, MyTree<Team> teams, MyTree<Participant> participants) {
        // Remove team from teamList
        teamList.remove(team);
    
        // Remove team from AVL Tree teams
        teams.delete(team);
    
        // Kumpulkan peserta yang akan dihapus
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
    
        // Bersihkan daftar peserta tim dan reset statistik tim
        team.participants = new MyLinkedList<>();
        team.memberCount = 0;
        team.totalPoints = 0;
        team.pointCounts = new MyTree<>(); // Inisialisasi ulang pointCounts
        // team.uniquePointCount = 0;
        team.participantTree = new MyTree<>(); // Inisialisasi ulang participantTree
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
        return participantsMap.getOrDefault(id, null);
    }
    
    
    private static void eliminateParticipant(Participant participant, Team team) {
        // Remove participant from AVL Tree participants
        participants.delete(participant);
    
        // Remove participant from team's participant list
        team.participants.remove(participant);
    
        // Update team member count
        team.memberCount--;
    
        // Remove participant from participantTree
        team.participantTree.delete(participant);
    
        // Update pointCounts and uniquePointCount
        if (team.pointCounts.contains(participant.points)) {
            team.pointCounts.delete(participant.points);
            // team.uniquePointCount--;
        }
    }
    

    private static void updateParticipantPoints(Participant participant, int pointChange) {
        Team team = participant.team;
    
        // Hapus peserta dari participantTree
        team.participantTree.delete(participant);
    
        // Update pointCounts dan uniquePointCount untuk poin yang lama
        if (team.pointCounts.contains(participant.points)) {
            team.pointCounts.delete(participant.points);
            // team.uniquePointCount--;
        }
    
        // Update poin peserta
        participant.points += pointChange;
    
        // Update totalPoints tim
        team.totalPoints += pointChange;
    
        if (participant.points > 0) {
            // Update pointCounts dan uniquePointCount untuk poin yang baru
            if (!team.pointCounts.contains(participant.points)) {
                // team.uniquePointCount++;
                team.pointCounts.insert(participant.points);
            }
    
            // Masukkan kembali peserta ke dalam participantTree dengan poin yang diperbarui
            team.participantTree.insert(participant);
        } else {
            // Jika poin peserta menjadi nol atau negatif, eliminasi mereka
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
        // out.println("Team list after sorting:");
        for (int i = 0; i < teamList.size(); i++) {
            Team team = teamList.get(i);
            out.println("Index " + i + ": Team ID: " + team.id + ", Total Points: " + team.totalPoints + ", Member Count: " + team.memberCount + ", Times Caught:" + team.timesCaught);
        }
    }

    private static Team getTeamById(int id) {
        // Lakukan pencarian pada linked list teamList
        MyLinkedList.Node<Team> currentNode = teamList.head;
        while (currentNode != null) {
            Team currentTeam = currentNode.data;
            if (currentTeam.id == id) {
                return currentTeam;
            }
            currentNode = currentNode.next;
        }
        // Jika tidak ditemukan, kembalikan null
        return null;
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

    // Custom Tree implementation using AVL Tree (without key-value)
    static class MyTree<E extends Comparable<E>> {
        private TreeNode<E> root;

        private static class TreeNode<E> {
            E value;
            TreeNode<E> left;
            TreeNode<E> right;
            int height;

            public TreeNode(E value) {
                this.value = value;
                this.height = 1;
            }
        }

        public void insert(E value) {
            root = insert(root, value);
        }

        public void delete(E value) {
            root = deleteNode(root, value);
        }

        public boolean contains(E value) {
            return getNode(root, value) != null;
        }

        public E get(E value) {
            TreeNode<E> node = getNode(root, value);
            return node == null ? null : node.value;
        }

        private TreeNode<E> getNode(TreeNode<E> node, E value) {
            if (node == null)
                return null;
            int cmp = value.compareTo(node.value);
            if (cmp < 0)
                return getNode(node.left, value);
            else if (cmp > 0)
                return getNode(node.right, value);
            else
                return node;
        }

        private TreeNode<E> insert(TreeNode<E> node, E value) {
            if (node == null)
                return new TreeNode<>(value);
            int cmp = value.compareTo(node.value);
            if (cmp < 0)
                node.left = insert(node.left, value);
            else if (cmp > 0)
                node.right = insert(node.right, value);
            else
                return node; // Duplicate values are not allowed
            node.height = 1 + Math.max(height(node.left), height(node.right));
            return balance(node);
        }

        private TreeNode<E> deleteNode(TreeNode<E> root, E value) {
            if (root == null)
                return root;
            int cmp = value.compareTo(root.value);
            if (cmp < 0)
                root.left = deleteNode(root.left, value);
            else if (cmp > 0)
                root.right = deleteNode(root.right, value);
            else {
                if ((root.left == null) || (root.right == null)) {
                    TreeNode<E> temp = null;
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
                    TreeNode<E> temp = minValueNode(root.right);
                    root.value = temp.value;
                    root.right = deleteNode(root.right, temp.value);
                }
            }
            if (root == null)
                return root;
            root.height = Math.max(height(root.left), height(root.right)) + 1;
            return balance(root);
        }

        private TreeNode<E> minValueNode(TreeNode<E> node) {
            TreeNode<E> current = node;
            while (current.left != null)
                current = current.left;
            return current;
        }

        private int height(TreeNode<E> node) {
            return node == null ? 0 : node.height;
        }

        private TreeNode<E> balance(TreeNode<E> y) {
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

        private int getBalance(TreeNode<E> node) {
            return node == null ? 0 : height(node.left) - height(node.right);
        }

        private TreeNode<E> rightRotate(TreeNode<E> y) {
            TreeNode<E> x = y.left;
            TreeNode<E> T2 = x.right;

            x.right = y;
            y.left = T2;

            y.height = Math.max(height(y.left), height(y.right)) + 1;
            x.height = Math.max(height(x.left), height(x.right)) + 1;

            return x;
        }

        private TreeNode<E> leftRotate(TreeNode<E> x) {
            TreeNode<E> y = x.right;
            TreeNode<E> T2 = y.left;

            y.left = x;
            x.right = T2;

            x.height = Math.max(height(x.left), height(x.right)) + 1;
            y.height = Math.max(height(y.left), height(y.right)) + 1;

            return y;
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