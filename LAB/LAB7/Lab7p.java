import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;


public class Lab7p {
    private static InputReader in;
    private static PrintWriter out;
    private static FilmHeap heap;
    private static ArrayList<Film> filmList;

    public static void main(String[] args) {
        InputStream inputStream = System.in;
        OutputStream outputStream = System.out;
        in = new InputReader(inputStream);
        out = new PrintWriter(outputStream);

        int N = in.nextInt();

        heap = new FilmHeap();
        filmList = new ArrayList<>();

        for (int i = 0; i < N; i++) {
            long vote = in.nextLong();
            Film film = new Film(vote);
            filmList.add(film);
            heap.insert(film);
        }

        int Q = in.nextInt();

        long vote;
        int film1, film2, filmNum;

        for (int i = 0; i < Q; i++) {
            String query = in.next();
            switch (query) {
                case "T":
                    vote = in.nextLong();
                    Film newFilm = new Film(vote);
                    filmList.add(newFilm);
                    heap.insert(newFilm);
                    Film minFilmT = heap.peek();
                    out.println(minFilmT.id + " " + minFilmT.vote);
                    break;
                case "V":
                    vote = in.nextLong();
                    Film minFilmV = heap.peek();
                    long oldVote = minFilmV.vote;
                    minFilmV.vote += vote;
                    heap.updateFilm(minFilmV.id, oldVote);
                    // After updating, need to get the new min film
                    Film newMinFilm = heap.peek();
                    out.println(newMinFilm.id + " " + newMinFilm.vote);
                    break;
                case "S":
                    filmNum = in.nextInt();
                    if (filmNum > heap.size) {
                        out.println("-1");
                    } else {
                        List<Film> films = heap.getKSmallest(filmNum);
                        for (int j = 0; j < films.size(); j++) {
                            out.print(films.get(j).id);
                            if (j != films.size() - 1) {
                                out.print(" ");
                            }
                        }
                        out.println();
                    }
                    break;
                case "B":
                    film1 = in.nextInt();
                    film2 = in.nextInt();
                    Film f1 = filmList.get(film1);
                    Film f2 = filmList.get(film2);

                    int cmp = f1.compareTo(f2);

                    Film winner, loser;

                    if (cmp < 0) { // f1 is less than f2
                        winner = f1;
                        loser = f2;
                    } else if (cmp > 0) {
                        winner = f2;
                        loser = f1;
                    } else {
                        if (f1.id < f2.id) {
                            winner = f1;
                            loser = f2;
                        } else {
                            winner = f2;
                            loser = f1;
                        }
                    }

                    out.println(winner.id);

                    long oldLoserVote = loser.vote;
                    loser.vote /= 2;
                    heap.updateFilm(loser.id, oldLoserVote);
                    break;
                default:
                    break;
            }
            // heap.traverse(); // heap traversal for debugging every after query
        }

        out.close();
    }

    static class Film implements Comparable<Film> {
        int id;
        long vote;
        static int idCounter;

        Film(long vote) {
            id = idCounter++;
            this.vote = vote;
        }

        // Implement compareTo method for comparison
        @Override
        public int compareTo(Film other) {
            if (this.vote != other.vote) {
                return Long.compare(this.vote, other.vote);
            } else {
                return Integer.compare(this.id, other.id);
            }
        }
    }

    static class FilmHeap {
        ArrayList<Film> heap;
        HashMap<Integer, Integer> idToIndexMap;
        int size;

        public FilmHeap() {
            heap = new ArrayList<>();
            idToIndexMap = new HashMap<>();
            size = 0;
        }

        public static int getParentIndex(int i) {
            return (i - 1) / 2;
        }

        public void percolateUp(int i) {
            while (i > 0) {
                int parent = getParentIndex(i);
                if (heap.get(i).compareTo(heap.get(parent)) < 0) {
                    swap(i, parent);
                    i = parent;
                } else {
                    break;
                }
            }
        }

        public void percolateDown(int i) {
            while (true) {
                int left = 2 * i + 1;
                int right = 2 * i + 2;
                int smallest = i;

                if (left < size && heap.get(left).compareTo(heap.get(smallest)) < 0) {
                    smallest = left;
                }

                if (right < size && heap.get(right).compareTo(heap.get(smallest)) < 0) {
                    smallest = right;
                }

                if (smallest != i) {
                    swap(i, smallest);
                    i = smallest;
                } else {
                    break;
                }
            }
        }

        public void updateFilm(int id, long oldVote) {
            int index = idToIndexMap.get(id);
            if (heap.get(index).vote < oldVote) {
                percolateUp(index);
            } else if (heap.get(index).vote > oldVote) {
                percolateDown(index);
            }
        }

        public void swap(int i, int j) {
            Film temp = heap.get(i);
            heap.set(i, heap.get(j));
            heap.set(j, temp);

            // Update the index mapping
            idToIndexMap.put(heap.get(i).id, i);
            idToIndexMap.put(heap.get(j).id, j);
        }

        public void insert(Film film) {
            heap.add(film);
            int index = size;
            idToIndexMap.put(film.id, index);
            size++;
            percolateUp(index);
        }

        public Film peek() {
            if (size == 0) {
                return null;
            }
            return heap.get(0);
        }

        public Film poll() {
            if (size == 0) {
                return null;
            }
            Film min = heap.get(0);
            Film last = heap.get(size - 1);
            heap.set(0, last);
            idToIndexMap.put(last.id, 0);
            heap.remove(size - 1);
            idToIndexMap.remove(min.id);
            size--;
            percolateDown(0);
            return min;
        }

        public List<Film> getKSmallest(int K) {
            List<Film> result = new ArrayList<>();
            if (size == 0) {
                return result;
            }

            // Nodes to visit, stored as indices
            ArrayList<Integer> nodesToVisit = new ArrayList<>();
            nodesToVisit.add(0);

            while (result.size() < K && !nodesToVisit.isEmpty()) {
                // Find the node with the smallest film among nodesToVisit
                int minIndex = 0;
                Film minFilm = heap.get(nodesToVisit.get(0));
                for (int i = 1; i < nodesToVisit.size(); i++) {
                    Film film = heap.get(nodesToVisit.get(i));
                    if (film.compareTo(minFilm) < 0) {
                        minFilm = film;
                        minIndex = i;
                    }
                }
                // Remove the selected node from nodesToVisit
                int currentIndex = nodesToVisit.remove(minIndex);
                // Add the film to the result
                result.add(minFilm);

                // Add its children to nodesToVisit
                int leftChildIndex = 2 * currentIndex + 1;
                int rightChildIndex = 2 * currentIndex + 2;

                if (leftChildIndex < size) {
                    nodesToVisit.add(leftChildIndex);
                }
                if (rightChildIndex < size) {
                    nodesToVisit.add(rightChildIndex);
                }
            }

            // Sort the result according to the problem's requirements
            Collections.sort(result);

            return result;
        }

        // =============== HELPER METHOD FOR DEBUGGING HEAP ===============
        public void traverse() {
            out.println("=============================");
            traverseHelper(0, 0);
            out.println("=============================");
        }

        // =============== HELPER METHOD FOR DEBUGGING HEAP ===============
        private void traverseHelper(int index, int depth) {
            if (index >= size) {
                return;
            }

            // Print the current node with indentation based on depth
            for (int i = 0; i < depth; i++) {
                out.print("  ");
            }
            out.println(heap.get(index).id + " (" + heap.get(index).vote + ")");

            // Traverse left and right children
            traverseHelper(2 * index + 1, depth + 1);
            traverseHelper(2 * index + 2, depth + 1);
        }
    }

    // taken from https://codeforces.com/submissions/Petr
    // together with PrintWriter, these input-output (IO) is much faster than the
    // usual Scanner(System.in) and System.out
    // please use these classes to avoid your fast algorithm gets Time Limit
    // Exceeded caused by slow input-output (IO)
    static class InputReader {
        public BufferedReader reader;
        public StringTokenizer tokenizer;

        public InputReader(InputStream stream) {
            reader = new BufferedReader(new InputStreamReader(stream), 32768);
            tokenizer = null;
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

        public char nextChar() {
            return next().charAt(0);
        }

        public int nextInt() {
            return Integer.parseInt(next());
        }

        public long nextLong()
        {
            return Long.parseLong(next());
        }
    }
}
