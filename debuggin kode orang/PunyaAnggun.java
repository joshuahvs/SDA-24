import java.io.*;
import java.util.StringTokenizer;
// import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

public class PunyaAnggun {
    private static CircularDoublyLinkedList teamList = new CircularDoublyLinkedList();
    private static TNode sofitaTeamNode = null;
    private static TNode jokiTeamNode = null; 
    private static int anggotaId = 1;

    static InputReader in = new InputReader(System.in);
    static PrintWriter out = new PrintWriter(System.out);

    public static void main(String[] args) {

        // Initialize variables
        int M = in.nextInt(); 
        int teamId = 1;

        int[] Mi = new int[M];
        for (int i = 0; i < M; i++) {
            Mi[i] = in.nextInt();
        }

        for (int i = 0; i < M; i++) {
            Team team = new Team(teamId);
            for (int j = 0; j < Mi[i]; j++) {
                int points = in.nextInt();
                if(points > 0){
                    Anggota anggota = new Anggota(anggotaId++, points);
                    team.addAnggota(anggota);
                }
            }
            teamList.append(team);
            teamId++;
        }

        if (teamList.getSize() > 0) {
            // out.println("print check 1 " +teamList.size);
            sofitaTeamNode = teamList.head;
            jokiTeamNode = findLowestPointTeam();

            if (jokiTeamNode != null && jokiTeamNode.data.id == sofitaTeamNode.data.id) {
                jokiTeamNode = findSecondLowestPointTeam();
            }
            
        }

        int Q = in.nextInt();
        for (int q = 0; q < Q; q++) {
            String command = in.next();
            switch (command) {
                case "A":
                    int count = in.nextInt();
                    String resultA = A(count, anggotaId);
                    if (!resultA.equals("-1")) {
                        anggotaId += count;
                    }
                    out.println(resultA);
                    break;
                case "M":
                    String arah = in.next();
                    String resultM = M(arah);
                    out.println(resultM);
                    break;
                case "J":
                    String direction = in.next();
                    String resultJ = J(direction);
                    out.println(resultJ);
                    break;
                case "G":
                    String teamDir = in.next();
                    String resultG = G(teamDir, teamId);
                    teamId++; 
                    out.println(resultG);
                    break;
                case "U":
                    String resultU = U();
                    out.println(resultU);
                    break;
                case "E":
                    int poin = in.nextInt();
                    String resultE = E(poin);
                    out.println(resultE);
                    break;
                case "B":
                    String bound = in.next();
                    String resultB = B(bound);
                    out.println(resultB);
                    break;
                case "T":
                    int id1 = in.nextInt();
                    int id2 = in.nextInt();
                    int point = in.nextInt();
                    String resultT = T(id1, id2, point);
                    out.println(resultT);
                    break;
                case "V":
                    int idA1 = in.nextInt();
                    int idA2 = in.nextInt();
                    int idTeam = in.nextInt();
                    int hasil = in.nextInt();
                    String resultV = V(idA1, idA2, idTeam, hasil);
                    out.println(resultV);
                    break;
                case "R":
                    String resultR = R();
                    out.println(resultR);
                    break;
                default:
                    break;
            }
            // printCurrentState();
        }

        out.close();
    }

    static void printCurrentState() {
        out.println("====== CURRENT STATE ======");
        out.println("Current Sofita Team: ID = " + (sofitaTeamNode != null ? sofitaTeamNode.data.id : "-1"));
        out.println("Current Jokey Team: ID = " + (jokiTeamNode != null ? jokiTeamNode.data.id : "-1"));
        
        // Iterasi melalui daftar tim dan cetak informasi masing-masing tim
        // if (teamList.head != null) {
        //     TNode current = teamList.head;
        //     do {
        //         Team team = current.data;
        //         out.println("Team ID: " + team.id);
        //         out.println("Total Points: " + team.totalPoin);
        //         out.println("Number of Participants: " + team.getSize());
        //         out.println("Participants (sorted by AVL Tree):");
                
        //         List<Anggota> sortedParticipants = team.anggota.inOrderTraversal();
        //         for (Anggota a : sortedParticipants) {
        //             out.println("  Participant ID: " + a.id + ",Match: " + a.pertandingan + ", Points: " + a.poin);
        //         }
                
        //         current = current.next;
        //     } while (current != teamList.head);
        // }
        
        // Cetak urutan tim
        out.println("Order of Teams:");
        if (teamList.head != null) {
            StringBuilder order = new StringBuilder();
            TNode temp = teamList.head;
            do {
                order.append("Team ").append(temp.data.id).append(" -> ");
                temp = temp.next;
            } while (temp != teamList.head);
            order.append("teamlist head: ").append(teamList.head.data.id);
            out.println(order.toString());
            out.println("teamlist tail: " + teamList.head.prev.data.id);
        }
        
        out.println("===========================");
    }

    static String A(int count, int anggotaId) {
        // printCurrentState();
        if (sofitaTeamNode == null) {
            return "-1";
        } else {
            for (int c = 0; c < count; c++) {
                Anggota newAnggota = new Anggota(anggotaId + c, 3);
                sofitaTeamNode.data.addAnggota(newAnggota);
            }
            sofitaTeamNode.data.updateTotalPoin();
            return String.valueOf(sofitaTeamNode.data.getSize());
        }
    }

    static String M(String direction) {
        // printCurrentState();
        if (teamList.getSize() <= 1 || sofitaTeamNode == null) {
            return "-1";
        }
    
        // Pindah ke kanan atau kiri
        sofitaTeamNode = direction.equals("R") ? sofitaTeamNode.next : sofitaTeamNode.prev;
    
        // Jika Sofita bertabrakan dengan Joki
        if (sofitaTeamNode.data.id == jokiTeamNode.data.id) {
            handleJoki();
        }
    
        // Jika semua tim dieliminasi
        if (sofitaTeamNode == null) {
            return "-1";
        }
        // printCurrentState();
    
        return String.valueOf(sofitaTeamNode.data.id);
    }
    
    
    

    static void handleJoki() {
        if (teamList.getSize() <= 1 || sofitaTeamNode == null || jokiTeamNode == null) {
            sofitaTeamNode = null;
            jokiTeamNode = null;
            return;
        }
    
        // Eliminasi tim Joki
        jokiTeamNode = null;
    
        // Eksekusi tim Sofita
        Team sofitaTeam = sofitaTeamNode.data;
        sofitaTeam.executeTim();
    
        // Jika tim Sofita dieliminasi, arahkan ke tim berikutnya
        if (sofitaTeam.isEliminated) {
            eliminateTeam(sofitaTeam);
            sofitaTeamNode = findHighestPointTeam();
        }
    
        // Jika hanya satu tim tersisa
        if (teamList.getSize() == 1) {
            sofitaTeamNode = null;
            jokiTeamNode = null;
            teamList.deleteAt(0);
            return;
        }
    
        // Tentukan tim Joki berikutnya
        moveJoki();
    
        // Pastikan Sofita tidak menunjuk ke tim yang sudah dieliminasi
        if (sofitaTeamNode != null && sofitaTeamNode.data.isEliminated) {
            sofitaTeamNode = findHighestPointTeam();
        }
    }
    
    
    
    static void eliminateTeam(Team team) {
        team.isEliminated = true;
        teamList.delete(team.id);
    }
    
    static void moveJoki() {
        if (teamList.getSize() <= 1) {
            jokiTeamNode = null;
            return;
        }
        TNode lowestTeamNode = findLowestPointTeam();
        if (lowestTeamNode.data.id == sofitaTeamNode.data.id) {
            lowestTeamNode = findSecondLowestPointTeam();
        }
    
        jokiTeamNode = lowestTeamNode;
    }
    
    // Metode untuk menemukan tim dengan poin tertinggi
    static TNode findHighestPointTeam() {
        TNode highestNode = teamList.head;
        if (highestNode == null) return null;
    
        TNode current = teamList.head.next;
        while (current != teamList.head) {
            if (current.data.totalPoin > highestNode.data.totalPoin ||
                (current.data.totalPoin == highestNode.data.totalPoin && current.data.id > highestNode.data.id)) {
                highestNode = current;
            }
            current = current.next;
        }
        return highestNode;
    }
    
    // Metode untuk menemukan tim dengan poin terendah
    static TNode findLowestPointTeam() {
        TNode lowestNode = teamList.head;
        if (lowestNode == null) return null;
    
        TNode current = teamList.head.next;
        while (current != teamList.head) {
            if (current.data.totalPoin < lowestNode.data.totalPoin ||
                (current.data.totalPoin == lowestNode.data.totalPoin && current.data.id < lowestNode.data.id)) {
                lowestNode = current;
            }
            current = current.next;
        }
        return lowestNode;
    }
    
    // Metode untuk menemukan tim dengan poin terendah kedua
    static TNode findSecondLowestPointTeam() {
        TNode lowestNode = findLowestPointTeam();
        TNode secondLowestNode = null;
    
        TNode current = teamList.head;
        do {
            if (current.data != lowestNode.data) {
                if (secondLowestNode == null ||
                    current.data.totalPoin < secondLowestNode.data.totalPoin ||
                    (current.data.totalPoin == secondLowestNode.data.totalPoin && current.data.id < secondLowestNode.data.id)) {
                    secondLowestNode = current;
                }
            }
            current = current.next;
        } while (current != teamList.head);
    
        return secondLowestNode;
    }
    
    

    static String J(String direction) {
        // printCurrentState();
        if (teamList.getSize() <= 1 || jokiTeamNode == null) {
            return "-1";
        }
        
        TNode targetNode = null;
        if (direction.equals("L")) {
            targetNode = jokiTeamNode.prev;
        } else if (direction.equals("R")) {
            targetNode = jokiTeamNode.next;
        }
    
        if (targetNode == null) {
            return "-1";
        }
    
        if (targetNode.data.id == sofitaTeamNode.data.id) {
            // handleJoki(); // Tangani konflik jika Joki mencoba pindah ke tim Sofita
        } else {
            jokiTeamNode = targetNode; // Pindahkan Joki ke node yang dituju
        }
    
        // printCurrentState();
        return String.valueOf(jokiTeamNode.data.id);
    }

    

    static String G(String teamDir, int teamId) {
        Team team = new Team(teamId);
        for (int i = 0; i < 7; i++) {
            Anggota a = new Anggota(anggotaId++, 1);
            team.addAnggota(a);
            // anggotaId++;
        }
        team.updateTotalPoin();

        if (sofitaTeamNode == null) { 
            teamList.append(team);
            sofitaTeamNode = teamList.head;
            jokiTeamNode = findLowestPointTeam();
        } else {
            if (teamDir.equals("L")) {
                teamList.insertAt(team, 0);
            } else if (teamDir.equals("R")) {
                teamList.insertAt(team, 1);
            }
            // **Perubahan:** Tidak mengubah jokiTeamNode saat menambahkan tim baru
        }

        // **Tambahkan Logika:** Hanya ubah jokiTeamNode jika joki dan sofita berada di tim yang sama
        if (jokiTeamNode != null && sofitaTeamNode.data.id == jokiTeamNode.data.id) {
            jokiTeamNode = findLowestPointTeam();
            if (jokiTeamNode != null && jokiTeamNode.data.id == sofitaTeamNode.data.id) {
                jokiTeamNode = findSecondLowestPointTeam();
            }
        }

        return String.valueOf(team.id);
    }
    

    static String U() {
        if (teamList.getSize() <= 1 || sofitaTeamNode == null) {
            return "-1";
        }

        int uniqueCount = sofitaTeamNode.data.poinUnik();
        return String.valueOf(uniqueCount);
    }

    static String E(int poin) {
        if (teamList.getSize() <= 1 || sofitaTeamNode == null) {
            return "-1";
        }
    
        List<Team> teamsToEliminate = new ArrayList<>();
        // out.println("print check 2 " + teamList.size);
    
        // out.println("teams to eliminate before = " + teamsToEliminate.size());
        // Step 1: Identify all teams to eliminate
        // for (Team team : teamList) {
        //     out.println(team);
        //     printCurrentState(); // Assuming this logs the current state for debugging
        //     out.println("total point team = " + team.totalPoin);
        //     if (!team.isEliminated && team.totalPoin < poin) {
        //         teamsToEliminate.add(team);
        //     }
        // }

        int counttest = 0;
        for (int i = 0; i< teamList.size; i++){
            if (teamList.get(i).totalPoin<poin){
                teamsToEliminate.add(teamList.get(i));
            }
            counttest++;
        }
        // out.println("teams to eliminate after = " + teamsToEliminate.size());

        // out.println("countest = "+ counttest);
    
        // Step 2: Count the number of teams to eliminate
        int count = teamsToEliminate.size();
    
        // Early exit if no teams to eliminate
        if (count == 0) {
            return "0";
        }
    
        // Step 3: Eliminate identified teams
        boolean sofitaEliminated = false;
        boolean jokiEliminated = false;
    
        for (Team team : teamsToEliminate) {
            // out.println(team.id); // Assuming this prints the team ID
            if (team == sofitaTeamNode.data) {
                sofitaEliminated = true;
            }
            if (team == jokiTeamNode.data) {
                jokiEliminated = true;
            }
            eliminateTeam(team); // Ensure this method correctly marks the team as eliminated and removes it from the list
        }
    
        // Step 4: Update Sofita and Joki Team Nodes if necessary
        if (sofitaEliminated && teamList.getSize() > 0) {
            sofitaTeamNode = findHighestPointTeam();
        }
    
        if (jokiEliminated && teamList.getSize() > 0) {
            TNode newJokiNode = findLowestPointTeam();
            if (newJokiNode != sofitaTeamNode) {
                jokiTeamNode = newJokiNode;
            } else {
                newJokiNode = findSecondLowestPointTeam();
                if (newJokiNode != null && newJokiNode.data.id != sofitaTeamNode.data.id) {
                    jokiTeamNode = newJokiNode;
                }
            }
        }
    
        // Step 5: Return the count of eliminated teams
        return String.valueOf(count);
    }
    
    
    static String B(String bound){
        if (teamList.getSize() <= 1 || sofitaTeamNode == null) {
            return "-1";
        }
        Team tempTeam = sofitaTeamNode.data;
        AVLTree<Anggota> anggota = tempTeam.anggota;
        int K = anggota.getSize();

        if (K < 1){
            return "0";
        }

        int indexQ1 = Math.max(0, (int) Math.floor((K-1) / 4.0));
        int indexQ3 = Math.min(K-1, (int) Math.floor((3 * (K-1)) / 4.0));

        int Q1 = anggota.getKthLargest(indexQ1+1).poin;
        int Q3 = anggota.getKthLargest(indexQ3+1).poin;

        int IQR = Q3 - Q1;
        int upperBound = Q3 + (int) (1.5 * IQR);
        int lowerBound = Q1 - (int) (1.5 * IQR);

        int count = 0;
        for (int i = 1; i <= K; ++i) {
            Anggota a = anggota.getKthLargest(i);
            if (bound.equals("U") && a.poin > upperBound) {
                count++;
            } else if (bound.equals("L") && a.poin < lowerBound) {
                count++;
            }
        }
        return String.valueOf(count);
    }

    static String T(int id1, int id2, int poin){
        if (teamList.getSize() <= 1 || sofitaTeamNode == null) {
            return "-1";
        }

        Team tempTeam = sofitaTeamNode.data;
        Anggota sender = tempTeam.getById(id1);
        Anggota receiver = tempTeam.getById(id2);

        if (sender == null || receiver == null || sender.poin <= poin) {
            return "-1";
        }

        tempTeam.updatePoin(sender, sender.poin - poin);
        tempTeam.updatePoin(receiver, receiver.poin + poin);

        if (tempTeam.getSize() < 7) {
            eliminateTeam(tempTeam);
        }

        if(sofitaTeamNode.data.id == tempTeam.id){
            Team maxTeam = null;
            for (int i = 0; i < teamList.getSize(); i++) {
                Team team = teamList.get(i);
                if (maxTeam == null || team.compareTo(maxTeam) < 0) {
                    maxTeam = team;
                    sofitaTeamNode.data.id = teamList.get(i).id;
                }
            }
        }

        if(jokiTeamNode.data.id == tempTeam.id){
            int tempLowest = Integer.MAX_VALUE;
            for (int i = 0; i < teamList.getSize(); i++) {
                if (teamList.get(i).id != sofitaTeamNode.data.id) {
                    Team temp = teamList.get(i);
                    if (jokiTeamNode == null|| temp.totalPoin < tempLowest) {
                        tempLowest = temp.totalPoin;
                        jokiTeamNode.data.id = temp.id;
                    } else if (temp.totalPoin == tempLowest) {
                        Team joki = jokiTeamNode.data;
                        if (temp.getSize() > joki.getSize()|| 
                            (temp.getSize() == joki.getSize() && temp.id > joki.id)) {
                            jokiTeamNode.data.id = temp.id;
                        }
                    }
                }
            }
        }
        if (jokiTeamNode.data.id == sofitaTeamNode.data.id) {
            handleJoki();
        }
        return sender.poin + " " + receiver.poin;
    }

    static String R() {
        // printCurrentState();
        if (teamList.getSize() <= 1 || sofitaTeamNode == null) {
            return "-1";
        }
    
        // Konversi linked list ke array untuk pengurutan
        int n = teamList.getSize();
        Team[] teams = new Team[n];
        TNode current = teamList.head;
        for (int i = 0; i < n; i++) {
            teams[i] = current.data;
            current = current.next;
        }
    
        // Panggil mergeSort
        mergeSort(teams, 0, n - 1);
    
        // Simpan ID tim Joki sebelum pengurutan
        int jokiTeamId = jokiTeamNode.data.id;
    
        // Buat linked list baru dengan urutan yang telah disorting
        CircularDoublyLinkedList sortedList = new CircularDoublyLinkedList();
        for (Team team : teams) {
            sortedList.append(team);
        }
    
        // Update referensi list
        teamList = sortedList;
    
        // Update posisi Joki (tetap di tim yang sama)
        current = teamList.head;
        do {
            if (current.data.id == jokiTeamId) {
                jokiTeamNode = current;
                break;
            }
            current = current.next;
        } while (current != teamList.head);
    
        // Posisi Sofita ke tim dengan peringkat tertinggi (paling kiri)
        sofitaTeamNode = teamList.head;
    
        // Jika Sofita dan Joki di tim yang sama
        if (sofitaTeamNode.data.id == jokiTeamNode.data.id) {
            handleJoki();
        }
    
        // Pastikan Sofita tidak berada di tim yang dieliminasi
        if (sofitaTeamNode.data.isEliminated) {
            TNode startNode = sofitaTeamNode;
            do {
                sofitaTeamNode = sofitaTeamNode.next;
                if (sofitaTeamNode == startNode) {
                    return "-1"; // Semua tim telah dieliminasi
                }
            } while (sofitaTeamNode.data.isEliminated);
        }
    
        // printCurrentState();
        return String.valueOf(sofitaTeamNode.data.id);
    }
    
    // Implementasi MergeSort
    static void mergeSort(Team[] arr, int left, int right) {
        if (left < right) {
            int mid = (left + right) / 2;
            mergeSort(arr, left, mid);
            mergeSort(arr, mid + 1, right);
            merge(arr, left, mid, right);
        }
    }
    
    static void merge(Team[] arr, int left, int mid, int right) {
        int n1 = mid - left + 1;
        int n2 = right - mid;
    
        Team[] L = new Team[n1];
        Team[] R = new Team[n2];
    
        // Copy data ke array temporary
        for (int i = 0; i < n1; i++)
            L[i] = arr[left + i];
        for (int j = 0; j < n2; j++)
            R[j] = arr[mid + 1 + j];
    
        int i = 0, j = 0;
        int k = left;
    
        while (i < n1 && j < n2) {
            // Implementasi kriteria pengurutan:
            // 1. Poin terbanyak
            // 2. Jika poin sama, jumlah anggota paling sedikit
            // 3. Jika masih sama, ID terkecil
            if ((R[j].compareTo(L[i])) >= 0) {
                arr[k] = L[i];
                i++;
            } else {
                arr[k] = R[j];
                j++;
            }
            k++;
        }
    
        // Copy sisa elemen dari L[] jika ada
        while (i < n1) {
            arr[k] = L[i];
            i++;
            k++;
        }
    
        // Copy sisa elemen dari R[] jika ada
        while (j < n2) {
            arr[k] = R[j];
            j++;
            k++;
        }
    }
    
    // Fungsi pembanding untuk dua tim
    static int compareTeams(Team a, Team b) {
        if (a.totalPoin != b.totalPoin) {
            return Integer.compare(a.totalPoin, b.totalPoin);
        }
        if (a.getSize() != b.getSize()) {
            return Integer.compare(b.getSize(), a.getSize());
        }
        return Integer.compare(b.id, a.id);
    }

    static String V(int id1, int id2, int idTeam, int hasil){
        if (teamList.getSize() <= 1 || sofitaTeamNode == null) {
            return "-1";
        }

        Team team1 = sofitaTeamNode.data;
        Team team2 = null;

        for (int i = 0; i < teamList.getSize(); i++) {
            if (teamList.get(i).id == idTeam) {
                team2 = teamList.get(i);
                break;
            }
        }

        if (team2 == null) {
            return "-1";
        }

        Anggota a1 = team1.getById(id1);
        Anggota a2 = team2.getById(id2);

        if (a1 == null || a2 == null) {
            return "-1";
        }

        String toReturn = "";

        if (hasil == 0) {
            a1.pertandingan++;
            a2.pertandingan++;

            team1.updatePoin(a1, a1.poin + 1);
            team2.updatePoin(a2, a2.poin + 1);

            toReturn = a1.poin + " " + a2.poin;

        } else if (hasil == 1) {
            a1.pertandingan++;
            a2.pertandingan++;

            team1.updatePoin(a1, a1.poin + 3);
            team2.updatePoin(a2, a2.poin - 3);
            toReturn = String.valueOf(a1.poin);
        } else if (hasil == -1) {
            a1.pertandingan++;
            a2.pertandingan++;

            team1.updatePoin(a1, a1.poin - 3);
            team2.updatePoin(a2, a2.poin + 3);
            toReturn = String.valueOf(a2.poin);
        }

        if (team1.isEliminated) {
            eliminateTeam(team1);
            if(sofitaTeamNode.data.id == team1.id){
                Team maxTeam = null;
                for (int i = 0; i < teamList.getSize(); i++) {
                    Team tmpTeam = teamList.get(i);
                    if (maxTeam == null || tmpTeam.compareTo(maxTeam) < 0) {
                        maxTeam = tmpTeam;
                        sofitaTeamNode.data.id = teamList.get(i).id;
                    }
                }
            }

            if(jokiTeamNode.data.id == team1.id){
                int tempLowest = Integer.MAX_VALUE;
                for (int i = 0; i < team1.getSize(); i++) {
                    if (teamList.get(i).id != sofitaTeamNode.data.id) {
                        Team tempTeam = teamList.get(i);
                        if (jokiTeamNode.data == null || tempTeam.totalPoin < tempLowest) {
                            tempLowest = tempTeam.totalPoin;
                            jokiTeamNode.data.id = tempTeam.id;
                        } else if (tempTeam.totalPoin == tempLowest) {
                            Team jokiTim = jokiTeamNode.data;
                            if (tempTeam.getSize() > jokiTim.getSize()|| 
                                (tempTeam.getSize() == jokiTim.getSize() && tempTeam.id > jokiTim.id)) {
                                jokiTeamNode.data.id = tempTeam.id;
                            }
                        }
                    }
                }
            }
        }
        if (team2.isEliminated) {
            eliminateTeam(team2);
            if(sofitaTeamNode.data.id == team2.id){
                Team maxTeam = null;
                for (int i = 0; i < teamList.getSize(); i++) {
                    Team tmpTeam = teamList.get(i);
                    if (maxTeam == null || tmpTeam.compareTo(maxTeam) < 0) {
                        maxTeam = tmpTeam;
                        sofitaTeamNode.data.id = teamList.get(i).id;
                    }
                }
            }

            if (jokiTeamNode.data.id == team2.id) {
                int tempLowest = Integer.MAX_VALUE;
                for (int i = 0; i < teamList.getSize(); i++) {
                    if (teamList.get(i).id != sofitaTeamNode.data.id) {
                        Team currentTim = teamList.get(i);
                        if (jokiTeamNode.data == null || currentTim.totalPoin < tempLowest) {
                            tempLowest = currentTim.totalPoin;
                            jokiTeamNode.data.id = currentTim.id;
                        } else if (currentTim.totalPoin == tempLowest) {
                            Team jokiTim = jokiTeamNode.data;
                            if (currentTim.getSize() > jokiTim.getSize() || 
                                (currentTim.getSize() == jokiTim.getSize() && currentTim.id > jokiTim.id)) {
                                jokiTeamNode.data.id = currentTim.id;
                            }
                        }
                    }
                }
            }
        }

        if (jokiTeamNode.data.id == sofitaTeamNode.data.id) {
            handleJoki();
        }

        return toReturn;

    }


    private static class InputReader {
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
                    if(line == null) return null;
                    tokenizer = new StringTokenizer(line);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            return tokenizer.nextToken();
        }

        public int nextInt() {
            return Integer.parseInt(next());
        }
    }

    static class Anggota implements Comparable<Anggota> {
        int id;
        int poin;
        int pertandingan;

        Anggota(int id, int poin) {
            this.id = id;
            this.poin = poin;
            this.pertandingan = 0; 
        }

        @Override
        public int compareTo(Anggota other) {
            if (this.poin != other.poin) {
                return other.poin - this.poin;
            }
            if (this.pertandingan != other.pertandingan) {
                return this.pertandingan - other.pertandingan;
            }
            return this.id - other.id;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Anggota)) return false;
            Anggota anggota = (Anggota) o;
            return id == anggota.id && poin == anggota.poin;
        }
    }

    static class Team implements Comparable<Team> {
        int id;
        AVLTree<Anggota> anggota;
        int totalPoin;
        boolean teamSofita;
        boolean isEliminated = false;
        
        // Menambahkan atribut totalKetahuan
        int totalKetahuan;
    
        Team(int id) {
            this.id = id;
            this.anggota = new AVLTree<>();
            this.totalPoin = 0;
            this.teamSofita = false;
            this.totalKetahuan = 0; // Inisialisasi
        }

        void addAnggota(Anggota anggota){
            this.anggota.insert(anggota);
            totalPoin += anggota.poin;
        }

        void removeAnggota(Anggota anggota) {
            this.anggota.delete(anggota);
            totalPoin -= anggota.poin;
        }

        void updateTotalPoin() {
            List<Anggota> list = this.anggota.inOrderTraversal();
            totalPoin = 0;
            for (Anggota a : list) {
                totalPoin += a.poin;
            }
        }

        void updatePoin(Anggota a, int newPoin) {
            this.totalPoin -= a.poin;
            if (newPoin <= 0) {
                anggota.delete(a);
            } else {
                a.poin = newPoin;
                this.totalPoin += newPoin;
            }   
        }

        void executeTim() {
            this.totalKetahuan++;
    
            if (this.totalKetahuan == 1) {
                // Menghapus 3 anggota teratas
                List<Anggota> topMembers = this.getTopMembers(3);
                for (Anggota anggota : topMembers) {
                    this.removeAnggota(anggota);
                }
                this.updateTotalPoin();
    
                // Mengeliminasi tim jika ukuran kurang dari 7
                if (this.getSize() < 7) {
                    this.isEliminated = true;
                }
            } else if (this.totalKetahuan == 2) {
                // Mereset semua poin anggota menjadi 1
                this.resetAllPoints();
                this.updateTotalPoin();
            } else if (this.totalKetahuan >= 3) {
                // Mengeliminasi tim dan mengatur ulang totalKetahuan
                this.isEliminated = true;
                this.totalKetahuan = 0;
            }
        }

        int getSize() {
            return this.anggota.getSize();
        }

        Anggota getById(int id) {
            List<Anggota> sortedAnggota = this.anggota.inOrderTraversal();
            for (Anggota anggota : sortedAnggota) {
                if (anggota.id == id) {
                    return anggota;
                }
            }
            return null;
        }

        List<Anggota> getTopMembers(int n) {
            List<Anggota> sortedList = this.anggota.inOrderTraversal();
            List<Anggota> topMembers = new ArrayList<>();
            for (int i = 0; i < Math.min(n, sortedList.size()); i++) {
                topMembers.add(sortedList.get(i));
            }
            return topMembers;
        }

        void resetAllPoints() {
            List<Anggota> list = this.anggota.inOrderTraversal();
            for (Anggota a : list) {
                anggota.delete(a);
                a.poin = 1;
                anggota.insert(a);
            }
        }

        int poinUnik() {
            List<Anggota> list = this.anggota.inOrderTraversal();
            Set<Integer> poinUnik = new HashSet<>();
            for (Anggota a : list) {
                poinUnik.add(a.poin);
            }
            return poinUnik.size();
        }

        @Override
        public int compareTo(Team other) {
            if (this.totalPoin == other.totalPoin) {
                if (this.getSize() == other.getSize()) {
                    return this.id - other.id;
                }
                return this.getSize() - other.getSize();
            }
            return other.totalPoin - this.totalPoin; // Perbaikan di sini
        }
    
    }

    static class TNode {
        Team data;
        TNode next;
        TNode prev;

        public TNode(Team data) {
            this.data = data;
            this.next = null;
            this.prev = null;
        }
    }

    static class CircularDoublyLinkedList implements Iterable<Team> {
        TNode head = null;
        private int size = 0;

        @Override
        public Iterator<Team> iterator() {
            return new Iterator<Team>() {
                private TNode current = null;
                private boolean first = true;

                @Override
                public boolean hasNext() {
                    return head != null && (first || current != head);
                }

                @Override
                public Team next() {
                    if (first) {
                        current = head;
                        first = false;
                    } else {
                        current = current.next;
                    }
                    return current.data;
                }
            };
        }

        public void append(Team data) {
            TNode newNode = new TNode(data);
            if (head == null) {
                head = newNode;
                newNode.next = newNode;
                newNode.prev = newNode;
            } else {
                TNode tail = head.prev;
                tail.next = newNode;
                newNode.prev = tail;
                newNode.next = head;
                head.prev = newNode;
            }
            size++;
        }

        public void prepend(Team data) {
            TNode newNode = new TNode(data);
            if (head == null) {
                head = newNode;
                newNode.next = newNode;
                newNode.prev = newNode;
            } else {
                TNode tail = head.prev;
                newNode.next = head;
                newNode.prev = tail;
                head.prev = newNode;
                tail.next = newNode;
                head = newNode;
            }
            size++;
        }

        public void insertBefore(Team data, int targetId) {
            TNode targetNode = findNodeById(targetId);
            TNode newNode = new TNode(data);
            newNode.next = targetNode;
            newNode.prev = targetNode.prev;
            targetNode.prev.next = newNode;
            targetNode.prev = newNode;
            size++;
        }

        public void insertAfter(Team data, int targetId) {
            TNode targetNode = findNodeById(targetId);
            TNode newNode = new TNode(data);
            newNode.prev = targetNode;
            newNode.next = targetNode.next;
            targetNode.next.prev = newNode;
            targetNode.next = newNode;
            size++;
        }

        private TNode findNodeById(int teamId) {
            if (head == null) return null;
            TNode current = head;
            do {
                if (current.data.id == teamId) {
                    return current;
                }
                current = current.next;
            } while (current != head);
            return null;
        }

        public void insertAt(Team data, int position) {
            if (position < 0 || position > size) {
                throw new IndexOutOfBoundsException("Invalid position");
            }

            if (position == 0) {
                prepend(data);
                return;
            }

            if (position == size) {
                append(data);
                return;
            }

            TNode current = head;
            for (int i = 0; i < position; ++i) {
                current = current.next;
            }

            TNode newNode = new TNode(data);
            newNode.next = current;
            newNode.prev = current.prev;
            current.prev.next = newNode;
            current.prev = newNode;
            size++;
        }
        public boolean delete(int teamId) {
            if (head == null) {
                return false;
            }
    
            TNode current = head;
            do {
                if (current.data.id == teamId) {
                    if (size == 1) {
                        head = null;
                    } else {
                        current.prev.next = current.next;
                        current.next.prev = current.prev;
                        if (current == head) {
                            head = current.next;
                        }
                    }
                    size--;
                    return true;
                }
                current = current.next;
            } while (current != head);
    
            return false;
        }
    
        public boolean deleteAt(int position) {
            if (position < 0 || position >= size) {
                return false;
            }
    
            if (size == 1) {
                head = null;
                size = 0;
                return true;
            }
    
            TNode current = head;
            for (int i = 0; i < position; ++i) {
                current = current.next;
            }
    
            current.prev.next = current.next;
            current.next.prev = current.prev;
            if (current == head) {
                head = current.next;
            }
            size--;
            return true;
        }
    
        // Search for a team by ID
        public Team search(int teamId) {
            return findNodeById(teamId) != null ? findNodeById(teamId).data : null;
        }
    
        public int getSize() {
            return size;
        }
    
        public boolean isEmpty() {
            return size == 0;
        }
    
        // Get team by position (0-based)
        public Team get(int position) {
            if (position < 0 || position >= size) {
                throw new IndexOutOfBoundsException("Invalid position");
            }
    
            TNode current = head;
            for (int i = 0; i < position; ++i) {
                current = current.next;
            }
            return current.data;
        }
    
        public int getPosition(int teamId) {
            if (head == null) {
                return -1;
            }
    
            TNode current = head;
            int position = 0;
            do {
                if (current.data.id == teamId) {
                    return position;
                }
                current = current.next;
                position++;
            } while (current != head);
    
            return -1;
        }
    }

    static class AVLTree<T extends Comparable<T>> {
        private class Node {
            T data;
            Node left, right;
            int height;
            int size;

            Node(T data) {
                this.data = data;
                this.height = 1;
                this.size = 1;
            }
        }

        private Node root;
        private int size = 0;

        public AVLTree() {
            this.root = null;
            this.size = 0;
        }

        public int getSize() {
            return size;
        }

        public void insert(T data) {
            root = insert(root, data);
            size++;
        }

        private Node insert(Node node, T data) {
            if (node == null) {
                return new Node(data);
            }

            if (data.compareTo(node.data) < 0) {
                node.left = insert(node.left, data);
            } else if (data.compareTo(node.data) > 0) {
                node.right = insert(node.right, data);
            } else {
                return node;
            }

            updateHeight(node);
            return balance(node);
        }

        public void delete(T data) {
            root = delete(root, data);
            if (root != null) {
                size--;
            }
        }

        private Node delete(Node node, T data) {
            if (node == null) {
                return null;
            }

            int compareResult = data.compareTo(node.data);
            if (compareResult < 0) {
                node.left = delete(node.left, data);
            } else if (compareResult > 0) {
                node.right = delete(node.right, data);
            } else {
                if (node.left == null)
                    return node.right;
                else if (node.right == null)
                    return node.left;
                node.data = getMin(node.right);
                node.right = delete(node.right, node.data);
            }

            updateHeight(node);
            return balance(node);
        }

        public T getMin() {
            if (root == null) {
                return null;
            }
            return getMin(root);
        }

        private T getMin(Node node) {
            if (node.left == null) {
                return node.data;
            }
            return getMin(node.left);
        }

        public T getMax() {
            if (root == null) {
                return null;
            }
            return getMax(root);
        }

        private T getMax(Node node) {
            if (node.right == null) {
                return node.data;
            }
            return getMax(node.right);
        }

        public T getKthLargest(int k) {
            if (k < 1 || k > size) {
                return null;
            }
            return getKthLargest(root, k);
        }

        private T getKthLargest(Node node, int k) {
            if (node == null) {
                return null;
            }

            int rightSize = (node.right != null) ? node.right.size : 0;

            if (k == rightSize + 1) {
                return node.data;
            } else if (k <= rightSize) {
                return getKthLargest(node.right, k);
            } else {
                return getKthLargest(node.left, k - rightSize - 1);
            }
        }

        public T search(T data) {
            Node result = search(root, data);
            return result == null ? null : result.data;
        }

        private Node search(Node node, T data) {
            if (node == null) {
                return null;
            }

            int compareResult = data.compareTo(node.data);
            if (compareResult == 0) {
                return node;
            } else if (compareResult < 0) {
                return search(node.left, data);
            } else {
                return search(node.right, data);
            }
        }

        private void updateHeight(Node node) {
            node.height = 1 + Math.max(height(node.left), height(node.right));
            node.size = 1 + size(node.left) + size(node.right);
        }

        private int height(Node node) {
            return node == null ? 0 : node.height;
        }

        private int size(Node node) {
            return node == null ? 0 : node.size;
        }

        private int getBalance(Node node) {
            return node == null ? 0 : height(node.left) - height(node.right);
        }

        private Node balance(Node node) {
            if (node == null) {
                return null;
            }

            int balance = getBalance(node);

            // Left heavy
            if (balance > 1) {
                if (getBalance(node.left) < 0) {
                    node.left = rotateLeft(node.left);
                }
                return rotateRight(node);
            }

            // Right heavy
            if (balance < -1) {
                if (getBalance(node.right) > 0) {
                    node.right = rotateRight(node.right);
                }
                return rotateLeft(node);
            }

            return node;
        }

        private Node rotateRight(Node y) {
            Node x = y.left;
            Node T2 = x.right;

            // Perform rotation
            x.right = y;
            y.left = T2;

            // Update heights and sizes
            updateHeight(y);
            updateHeight(x);

            // Return new root
            return x;
        }

        private Node rotateLeft(Node x) {
            Node y = x.right;
            Node T2 = y.left;

            // Perform rotation
            y.left = x;
            x.right = T2;

            // Update heights and sizes
            updateHeight(x);
            updateHeight(y);

            // Return new root
            return y;
        }

        public List<T> inOrderTraversal() {
            List<T> result = new ArrayList<>();
            inOrderTraversal(root, result);
            return result;
        }

        private void inOrderTraversal(Node node, List<T> result) {
            if (node != null) {
                inOrderTraversal(node.left, result);
                result.add(node.data);
                inOrderTraversal(node.right, result);
            }
        }
    }
}