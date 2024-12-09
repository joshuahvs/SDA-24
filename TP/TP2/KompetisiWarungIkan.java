import java.util.*;

public class KompetisiWarungIkan {
    static class Peserta {
        int id, poin, pertandingan;

        Peserta(int id, int poin) {
            this.id = id;
            this.poin = poin;
            this.pertandingan = 0;
        }
    }

    static class Tim {
        int id;
        List<Peserta> pesertaList = new ArrayList<>();
        int poinTotal = 0;
        int pelanggaranPenjoki = 0;

        Tim(int id) {
            this.id = id;
        }

        void tambahPeserta(Peserta p) {
            pesertaList.add(p);
            poinTotal += p.poin;
        }

        void hitungPoinTotal() {
            poinTotal = pesertaList.stream().mapToInt(p -> p.poin).sum();
        }

        void sortirPeserta() {
            pesertaList.sort(Comparator.comparingInt((Peserta p) -> -p.poin)
                .thenComparingInt(p -> p.pertandingan)
                .thenComparingInt(p -> p.id));
        }
    }

    static class Kompetisi {
        List<Tim> timList = new ArrayList<>();
        Map<Integer, Tim> idKeTim = new HashMap<>();
        Map<Integer, Peserta> idKePeserta = new HashMap<>();
        Tim timDiawasi;
        Tim timPenjoki;

        int idPesertaTerakhir = 1;

        Kompetisi(int jumlahTim, int[] jumlahPesertaPerTim, int[] poinPeserta) {
            for (int i = 0; i < jumlahTim; i++) {
                Tim tim = new Tim(i + 1);
                for (int j = 0; j < jumlahPesertaPerTim[i]; j++) {
                    Peserta peserta = new Peserta(idPesertaTerakhir++, poinPeserta[j]);
                    tim.tambahPeserta(peserta);
                    idKePeserta.put(peserta.id, peserta);
                }
                tim.sortirPeserta();
                timList.add(tim);
                idKeTim.put(tim.id, tim);
            }
            timDiawasi = timList.get(0);
            timPenjoki = timList.get(timList.size() - 1);
        }

        void perintahA(int jumlahPeserta) {
            for (int i = 0; i < jumlahPeserta; i++) {
                Peserta pesertaBaru = new Peserta(idPesertaTerakhir++, 3);
                timDiawasi.tambahPeserta(pesertaBaru);
                idKePeserta.put(pesertaBaru.id, pesertaBaru);
            }
            timDiawasi.sortirPeserta();
            System.out.println(timDiawasi.pesertaList.size());
        }

        void perintahM(String arah) {
            int posisiSaatIni = timList.indexOf(timDiawasi);
            if (arah.equals("L")) {
                timDiawasi = (posisiSaatIni == 0) ? timList.get(timList.size() - 1) : timList.get(posisiSaatIni - 1);
            } else if (arah.equals("R")) {
                timDiawasi = (posisiSaatIni == timList.size() - 1) ? timList.get(0) : timList.get(posisiSaatIni + 1);
            }
            System.out.println(timDiawasi.id);
        }

        void perintahJ(String arah) {
            int posisiPenjoki = timList.indexOf(timPenjoki);
            if (arah.equals("L")) {
                timPenjoki = (posisiPenjoki == 0) ? timList.get(timList.size() - 1) : timList.get(posisiPenjoki - 1);
            } else if (arah.equals("R")) {
                timPenjoki = (posisiPenjoki == timList.size() - 1) ? timList.get(0) : timList.get(posisiPenjoki + 1);
            }
            if (timPenjoki == timDiawasi) {
                System.out.println(timPenjoki.id);
            } else {
                System.out.println(-1);
            }
        }

        void perintahE(int poinBatas) {
            int totalDieliminasi = 0;
            for (Iterator<Tim> iterator = timList.iterator(); iterator.hasNext();) {
                Tim tim = iterator.next();
                tim.hitungPoinTotal();
                if (tim.poinTotal < poinBatas) {
                    iterator.remove();
                    idKeTim.remove(tim.id);
                    totalDieliminasi++;
                }
            }
            timDiawasi = timList.stream().max(Comparator.comparingInt(t -> t.poinTotal)).orElse(null);
            System.out.println(totalDieliminasi);
        }

        void perintahT(int idPengirim, int idPenerima, int poinDikirim) {
            Peserta pengirim = idKePeserta.get(idPengirim);
            Peserta penerima = idKePeserta.get(idPenerima);

            if (pengirim == null || penerima == null || !timDiawasi.pesertaList.contains(pengirim)
                    || !timDiawasi.pesertaList.contains(penerima)) {
                System.out.println(-1);
                return;
            }
            if (poinDikirim >= pengirim.poin) {
                System.out.println(-1);
                return;
            }
            pengirim.poin -= poinDikirim;
            penerima.poin += poinDikirim;
            timDiawasi.sortirPeserta();
            System.out.println(pengirim.poin + " " + penerima.poin);
        }

        void perintahU() {
            Set<Integer> poinUnik = new HashSet<>();
            for (Peserta p : timDiawasi.pesertaList) {
                poinUnik.add(p.poin);
            }
            System.out.println(poinUnik.size());
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int jumlahTim = scanner.nextInt();
        int[] jumlahPesertaPerTim = new int[jumlahTim];
        for (int i = 0; i < jumlahTim; i++) {
            jumlahPesertaPerTim[i] = scanner.nextInt();
        }
        int totalPeserta = Arrays.stream(jumlahPesertaPerTim).sum();
        int[] poinPeserta = new int[totalPeserta];
        for (int i = 0; i < totalPeserta; i++) {
            poinPeserta[i] = scanner.nextInt();
        }
        Kompetisi kompetisi = new Kompetisi(jumlahTim, jumlahPesertaPerTim, poinPeserta);

        int jumlahPerintah = scanner.nextInt();
        for (int i = 0; i < jumlahPerintah; i++) {
            String perintah = scanner.next();
            switch (perintah) {
                case "A":
                    int jumlahPeserta = scanner.nextInt();
                    kompetisi.perintahA(jumlahPeserta);
                    break;
                case "M":
                    String arah = scanner.next();
                    kompetisi.perintahM(arah);
                    break;
                case "J":
                    String arahPenjoki = scanner.next();
                    kompetisi.perintahJ(arahPenjoki);
                    break;
                case "E":
                    int poinBatas = scanner.nextInt();
                    kompetisi.perintahE(poinBatas);
                    break;
                case "T":
                    int idPengirim = scanner.nextInt();
                    int idPenerima = scanner.nextInt();
                    int poinDikirim = scanner.nextInt();
                    kompetisi.perintahT(idPengirim, idPenerima, poinDikirim);
                    break;
                case "U":
                    kompetisi.perintahU();
                    break;
                default:
                    break;
            }
        }
        scanner.close();
    }
}
