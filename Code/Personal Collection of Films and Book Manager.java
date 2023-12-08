package org.example;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import javax.imageio.ImageIO;
import javax.swing.event.HyperlinkEvent;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.*;

public class CollectionManager extends JFrame {
    private static final String OMDB_API_KEY = "3fda16ad";
    private static final String OMDB_API_URL = "http://www.omdbapi.com/";
    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/collectmanagerdatabase2";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "zxcvbnm123";
    private static final int WIDTH = 1280;
    private static final int HEIGHT = 800;
    private static final int MOVIE_POSTER_WIDTH = 200;
    private static final int MOVIE_POSTER_HEIGHT = 280;
    private static final int BOOK_COVER_WIDTH = 200;   // Adjust as needed
    private static final int BOOK_COVER_HEIGHT = 280;  // Adjust as needed
    private static final String[] MOVIE_COLUMNS = {"Постер","Описание"};
    private static final String[] BOOK_COLUMNS = {"Постер","Описание"};
    private static final int numberOfMoviesToDisplay = 3;
    private static final int numberOfBooksToDisplay = 2;
    private Connection connection;
    private String username;
    private JPanel detailsPanel;
    private JTextArea descriptionArea;
    private JTable table;
    final String title = "Название";
    final String identifier = "ID";
    private DefaultTableModel model;
    private JTextField searchField;
    private JButton loadMoviesButton;
    private JButton loadBooksButton;
    private JButton removeFromFavoritesButton;
    private MovieDetails selectedMovie;
    private BookDetails selectedBook;
    private JButton addToFavoritesButton;
    private JButton viewFavoritesButton;
    private List<MovieDetails> movies;
    private List<BookDetails> books;
    private List<MovieDetails> favoriteMovies;
    private List<BookDetails> favoriteBooks;
    public CollectionManager() {
        super("Менеджер личной коллекции фильмов и книг");

        setSize(WIDTH, HEIGHT);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel(new BorderLayout());
        descriptionArea = new JTextArea();
        model = new DefaultTableModel(MOVIE_COLUMNS, 0);
        table = new JTable(model) {
            @Override
            public Class getColumnClass(int column) {
                if (column == 0) {
                    return ImageIcon.class;
                }
                return Object.class;
            }
        };
        JScrollPane tableScrollPane = new JScrollPane(table);
        panel.add(tableScrollPane, BorderLayout.CENTER);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true);

        table.setRowHeight(400);

        table.getSelectionModel().addListSelectionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow >= 0) {
                if (table.getColumnCount() == MOVIE_COLUMNS.length) {
                    if (!movies.isEmpty()) {
                        selectedMovie = movies.get(selectedRow);
                        displayMovieDetails(selectedMovie);
                    }
                } else if (table.getColumnCount() == BOOK_COLUMNS.length) {
                    if (!books.isEmpty()) {
                        selectedBook = books.get(selectedRow);
                        displayBookDetails(selectedBook);
                    }
                }
            }
        });
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        loadMoviesButton = new JButton("Загрузить фильмы");
        loadMoviesButton.addActionListener(e -> loadMovies());
        buttonPanel.add(loadMoviesButton);

        loadBooksButton = new JButton("Загрузить книги");
        loadBooksButton.addActionListener(e -> loadBooksFromApi());
        buttonPanel.add(loadBooksButton);
        viewFavoritesButton = new JButton("Просмотреть избранное");
        viewFavoritesButton.addActionListener(e -> viewFavorites());
        buttonPanel.add(viewFavoritesButton);
        panel.add(buttonPanel, BorderLayout.WEST);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        searchField = new JTextField(20);
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                searchCollection();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                searchCollection();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                searchCollection();
            }
        });
        searchPanel.add(searchField);
        panel.add(searchPanel, BorderLayout.NORTH);
        JPanel favoriteButtonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        addToFavoritesButton = new JButton("Добавить в избранное");
        addToFavoritesButton.addActionListener(e -> addToFavorites());
        favoriteButtonsPanel.add(addToFavoritesButton);
        removeFromFavoritesButton = new JButton("Удалить из избранного");
        removeFromFavoritesButton.addActionListener(e -> removeFromFavorites(username,title,identifier));
        favoriteButtonsPanel.add(removeFromFavoritesButton);
        panel.add(favoriteButtonsPanel, BorderLayout.SOUTH);
        favoriteButtonsPanel.add(Box.createHorizontalGlue());
        favoriteButtonsPanel.add(Box.createHorizontalStrut(10));
        panel.add(favoriteButtonsPanel, BorderLayout.SOUTH);
        detailsPanel = new JPanel(new BorderLayout());
        descriptionArea = new JTextArea(); // Добавлена инициализация переменной
        JScrollPane scrollPane = new JScrollPane(descriptionArea);
        detailsPanel.add(scrollPane);

        add(panel);
        setVisible(true);
        initializeDatabaseConnection();
        initializeData();
        // Запрос имени пользователя
        username = JOptionPane.showInputDialog("Введите ваше имя пользователя:");
    }

    private void initializeData() {
        movies = new ArrayList<>();
        books = new ArrayList<>();
        favoriteMovies = new ArrayList<>();
        favoriteBooks = new ArrayList<>();
    }

    private void initializeDatabaseConnection() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASSWORD);
            System.out.println("Connected to the database!");
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Ошибка подключения к базе данных. Подробности в консоли.");
            System.exit(1);
        }
    }

    private void removeFromFavorites(String type, String title, String identifier) {
        String tableName = (type.equalsIgnoreCase("movie")) ? "favorite_movies" : "favorite_books";

        try (Connection connection = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASSWORD)) {
            String query = String.format("DELETE FROM %s WHERE username = ? AND %s_title = ? AND %s_id = ?", tableName, type, type);
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, username);
                statement.setString(2, title);
                statement.setString(3, identifier);

                int affectedRows = statement.executeUpdate();
                if (affectedRows > 0) {
                    JOptionPane.showMessageDialog(null, type.substring(0, 1).toUpperCase() + type.substring(1) + " удален(а) из избранного!");
                } else {
                    JOptionPane.showMessageDialog(null, type.substring(0, 1).toUpperCase() + type.substring(1) + " не найден(а) в избранном.");
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Ошибка при удалении из избранного.\n" + ex.getMessage());
        }
    }
    private void addToFavorites() {
        int selectedRow = table.getSelectedRow();
        try {
            String sql;
            PreparedStatement statement;
            if (table.getColumnCount() == MOVIE_COLUMNS.length) {
                selectedMovie = movies.get(selectedRow);
                sql = "INSERT INTO favorite_movies (username, title, imdb_id, poster, summary) VALUES (?, ?, ?, ?, ?)";
                statement = connection.prepareStatement(sql);
                statement.setString(1, username);
                statement.setString(2, selectedMovie.getTitle());
                statement.setString(3, selectedMovie.getOmdbid());
                statement.setString(4, selectedMovie.getPoster());
                statement.setString(5, selectedMovie.getSummary());
            } else if (table.getColumnCount() == BOOK_COLUMNS.length) {
                selectedBook = books.get(selectedRow);
                sql = "INSERT INTO favorite_books (username, title, author, cover_url, summary) VALUES (?, ?, ?, ?, ?)";
                statement = connection.prepareStatement(sql);
                statement.setString(1, username);
                statement.setString(2, selectedBook.getTitle());
                statement.setString(3, selectedBook.getAuthor());
                statement.setString(4, selectedBook.getCoverUrl());
                statement.setString(5, selectedBook.getSummary());
            } else {
                JOptionPane.showMessageDialog(this, "Неизвестный тип данных в таблице.");
                return;
            }

            int rowsInserted = statement.executeUpdate();
            if (rowsInserted > 0) {
                JOptionPane.showMessageDialog(this, "Добавлено в избранное!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Ошибка при добавлении в избранное. Подробности в консоли.");
        }
    }
    private void viewFavorites() {
        try {
            String sql = "SELECT * FROM favorite_movies WHERE LOWER(username) = LOWER(?)";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, username);

                ResultSet resultSet = statement.executeQuery();
                model.setColumnIdentifiers(MOVIE_COLUMNS);
                model.setRowCount(0);

                while (resultSet.next()) {
                    String title = resultSet.getString("title");
                    String poster = resultSet.getString("poster");
                    String summary = resultSet.getString("summary");

                    MovieDetails favoriteMovie = new MovieDetails(title, poster, "", "", "");
                    favoriteMovie.setSummary(summary);

                    ImageIcon moviePosterIcon = favoriteMovie.getPoster() != null ?
                            new ImageIcon(getImage(favoriteMovie.getPoster(), MOVIE_POSTER_WIDTH, MOVIE_POSTER_HEIGHT)) : new ImageIcon();

                    Object[] data = {moviePosterIcon, favoriteMovie.getSummary()};
                    model.addRow(data);
                }
            }

            int selectedRow = table.getSelectedRow();
            if (selectedRow >= 0 && selectedRow < model.getRowCount()) {
                selectedMovie = new MovieDetails("", "", "", "", "");
                selectedMovie.setSummary(model.getValueAt(selectedRow, 1).toString());
                displayMovieDetails(selectedMovie);
            } else {
                selectedMovie = null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Ошибка при просмотре избранного. Подробности в консоли.");
        }
    }

    private void loadMovies() {
        String randomGenre = getRandomGenre();
        List<MovieDetails> movieDetailsList = fetchMoviesFromOMDB(randomGenre);

        model.setColumnIdentifiers(MOVIE_COLUMNS);
        model.setRowCount(0);

        for (int i = 0; i < Math.min(numberOfMoviesToDisplay, movieDetailsList.size()); i++) {
            MovieDetails movieDetails = movieDetailsList.get(i);
            movies.add(movieDetails);

            // Проверка на наличие постера перед созданием ImageIcon
            ImageIcon moviePosterIcon = movieDetails.getPoster() != null ?
                    new ImageIcon(getImage(movieDetails.getPoster(), MOVIE_POSTER_WIDTH, MOVIE_POSTER_HEIGHT)) : new ImageIcon();

            Object[] data = {moviePosterIcon, movieDetails.getSummary()};
            model.addRow(data);
        }

        if (!movieDetailsList.isEmpty()) {
            loadMovieDetails(movieDetailsList.get(0));
        }

        // Выведите отладочную информацию
        for (MovieDetails movie : movieDetailsList) {
            System.out.println("Movie: " + movie.getTitle() + ", Poster: " + movie.getPoster() + ", Plot: " + movie.getPlot() + ", Summary: " + movie.getSummary());
        }
    }
    private void updateDescriptionPane(JTextPane descriptionPane, String imdbID) {
        try {
            OkHttpClient client = new OkHttpClient();
            String url = OMDB_API_URL + "?apikey=" + OMDB_API_KEY + "&i=" + imdbID;

            Request request = new Request.Builder()
                    .url(url)
                    .build();

            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                String responseData = response.body().string();
                JSONObject jsonObject = new JSONObject(responseData);

                String plot = jsonObject.getString("Plot");
                String imdbLink = "IMDb: <a href='https://www.imdb.com/title/" + imdbID + "'>IMDb Page</a>";

                String htmlText = "<html><body>" + "Plot: " + plot + "<br><br>" + imdbLink + "</body></html>";

                descriptionPane.setContentType("text/html");
                descriptionPane.setText(htmlText);

                descriptionPane.addHyperlinkListener(e -> {
                    if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                        try {
                            Desktop.getDesktop().browse(e.getURL().toURI());
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                });
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Ошибка при загрузке описания.\n" + ex.getMessage());
        }
    }
    private void loadMovieDetails(MovieDetails movieDetails) {
        JTextPane descriptionPane = new JTextPane();
        updateDescriptionPane(descriptionPane, movieDetails.getOmdbid());

        displayMovieDetails(movieDetails);

        // Добавьте обработчик гиперссылок для открытия IMDb страницы
        descriptionPane.addHyperlinkListener(e -> {
            if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                try {
                    Desktop.getDesktop().browse(e.getURL().toURI());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }
    private void loadBooksFromApi() {
        String query = searchField.getText().trim();

        if (query.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Введите поисковый запрос для загрузки книг.");
            return;
        }

        try {
            System.out.println("Loading books...");
            System.out.println("Fetching books from API for query: " + query);

            List<BookDetails> bookDetailsList = fetchBooksFromAPI(query);

            System.out.println("Received books API response: " + bookDetailsList);

            model.setColumnIdentifiers(BOOK_COLUMNS);
            model.setRowCount(0);

            for (int i = 0; i < Math.min(numberOfBooksToDisplay, bookDetailsList.size()); i++) {
                BookDetails bookDetails = bookDetailsList.get(i);
                books.add(bookDetails);

                Image bookCover = getImage(bookDetails.getCoverUrl(), BOOK_COVER_WIDTH, BOOK_COVER_HEIGHT);
                if (bookCover != null) {
                    Object[] data = {new ImageIcon(bookCover), bookDetails.getTitle()};
                    model.addRow(data);
                } else {
                    Object[] data = {null, bookDetails.getTitle()};
                    model.addRow(data);
                }
            }

            if (!bookDetailsList.isEmpty()) {
                displayBookDetails(bookDetailsList.get(0));
            }

            System.out.println("Fetched books from API: " + bookDetailsList);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Ошибка при загрузке книг. Подробности в консоли.");
        }
    }
    private String getRandomGenre() {
        String[] genres = {"comedy", "action", "drama", "thriller", "fantasy", "Lord", "Cry"};
        int randomIndex = (int) (Math.random() * genres.length);
        return genres[randomIndex];
    }

    private List<MovieDetails> fetchMoviesFromOMDB(String genre) {
        System.out.println("Fetching books from API...");
        List<MovieDetails> movieList = new ArrayList<>();

        try {
            String omdbUrl = OMDB_API_URL + "?apikey=" + OMDB_API_KEY + "&type=movie&s=" + genre;

            URL url = new URL(omdbUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                reader.close();

                JSONObject jsonResponse = new JSONObject(response.toString());
                JSONArray movies = jsonResponse.getJSONArray("Search");

                for (int i = 0; i < movies.length(); i++) {
                    JSONObject movie = movies.getJSONObject(i);
                    String movieTitle = movie.getString("Title");
                    String moviePoster = movie.getString("Poster");
                    String omdbID = movie.getString("imdbID");

                    MovieDetails movieDetails = new MovieDetails(movieTitle, moviePoster, "", "", omdbID);


                    JTextPane descriptionPane = new JTextPane();
                    updateDescriptionPane(descriptionPane, omdbID);

                    movieDetails.setSummary(descriptionPane.getText());
                    movieList.add(movieDetails);
                }

            } else {
                System.err.println("Failed to fetch movies. HTTP Error Code: " + responseCode);
            }
            connection.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return movieList;
    }
    private List<BookDetails> fetchBooksFromAPI(String query) {
        List<BookDetails> bookList = new ArrayList<>();

        try {
            String apiUrl = "https://openlibrary.org/search.json?q=" + URLEncoder.encode(query, StandardCharsets.UTF_8.toString());
            URL url = new URL(apiUrl);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                reader.close();

                JSONObject jsonResponse = new JSONObject(response.toString());
                JSONArray docs = jsonResponse.getJSONArray("docs");

                for (int i = 0; i < docs.length(); i++) {
                    JSONObject doc = docs.getJSONObject(i);
                    String title = doc.optString("title", "");
                    JSONArray authorsArray = doc.optJSONArray("author_name");
                    String author = (authorsArray != null && authorsArray.length() > 0) ? authorsArray.getString(0) : "";
                    String coverId = doc.optString("cover_i", "");
                    
                    String coverUrl = "https://covers.openlibrary.org/b/id/" + coverId + "-L.jpg";

                    BookDetails bookDetails = new BookDetails(title, author, coverUrl, "", "","");
                    bookList.add(bookDetails);
                }
            } else {
                System.err.println("Failed to fetch books. HTTP Error Code: " + responseCode);
            }

            connection.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return bookList;
    }
    private Image getImage(String imageUrl, int width, int height) {
        try {
            URL url = new URL(imageUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                Image originalImage = ImageIO.read(connection.getInputStream());

                if (originalImage != null) {
                    return originalImage.getScaledInstance(width, height, Image.SCALE_SMOOTH);
                } else {
                    System.err.println("Ошибка при загрузке изображения. ImageIO.read() вернул null.");
                }
            } else {
                System.err.println("Ошибка при загрузке изображения. HTTP Error Code: " + responseCode);
                // Здесь вы можете выполнить дополнительные действия при ошибке 404, например, вывести сообщение об ошибке
            }

            connection.disconnect();
        } catch (IOException e) {
            System.err.println("Ошибка при загрузке изображения по URL: " + imageUrl);
            e.printStackTrace();
        }

        return null;
    }
    private void displayMovieDetails(MovieDetails movie) {
        String details = "Название: " + movie.getTitle() + "\n" +
                "Постер: " + movie.getPoster() + "\n" +
                "Краткое содержание: " + movie.getSummary();
        descriptionArea.setText(details);
        updateDetailsPanel(details); // Обновляем детали в панели
    }
    private void displayBookDetails(BookDetails book) {
        System.out.println("Displaying book details: " + book.getTitle());
        String details = "Название: " + book.getTitle() + "\n";
        updateDetailsPanel(details);
    }

    private void updateDetailsPanel(String details) {
        System.out.println("Updating details panel with: " + details);
        SwingUtilities.invokeLater(() -> {
            detailsPanel.removeAll();
            JTextArea detailsArea = new JTextArea(details);
            detailsArea.setEditable(false);
            detailsPanel.add(detailsArea);
            detailsPanel.revalidate();
            detailsPanel.repaint();

            System.out.println("Updated details panel with: " + details);
        });
    }
    private void searchCollection() {
        String searchText = searchField.getText().toLowerCase();

        model.setRowCount(0);

        for (MovieDetails movie : movies) {
            if (movie.getTitle().toLowerCase().contains(searchText)) {
                ImageIcon moviePosterIcon = movie.getPoster() != null ?
                        new ImageIcon(getImage(movie.getPoster(), MOVIE_POSTER_WIDTH, MOVIE_POSTER_HEIGHT)) : new ImageIcon();
                Object[] data = {moviePosterIcon, movie.getSummary()};
                model.addRow(data);
            }
        }

        for (BookDetails book : books) {
            if (book.getTitle().toLowerCase().contains(searchText) || book.getAuthor().toLowerCase().contains(searchText)) {
                Image bookImage = book.getCoverUrl() != null ?
                        getImage(book.getCoverUrl(), BOOK_COVER_WIDTH, BOOK_COVER_HEIGHT) : null;

                ImageIcon bookCoverIcon = (bookImage != null) ? new ImageIcon(bookImage) : new ImageIcon();

                int width = 240;
                int height = 280;
                Image resizedImage = (bookImage != null) ? bookImage.getScaledInstance(width, height, Image.SCALE_SMOOTH) : null;

                Object[] data = {new ImageIcon(resizedImage), book.getSummary()};
                model.addRow(data);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(CollectionManager::new);
    }
}
package org.example;
public class BookDetails {
    private String title;
    private String author;
    private String coverUrl;
    private String bookId;
    private String description;
    private String rating;
    private String summary;
    private String openLibraryId;

    public BookDetails(String title, String author, String coverUrl, String description, String openLibraryId, String summary) {
        this.title = title;
        this.author = author;
        this.coverUrl = coverUrl;
        this.description = description;
        this.openLibraryId = openLibraryId;
        this.summary=summary;
    }
    public void setSummary(String summary) {
        this.summary = summary;
    }
    public String getSummary() {
        return summary;
    }
    public String getBookId() {
        return bookId;
    }
    public String getOpenLibraryId(){
        return openLibraryId;
    }

    public void setBookId(String bookId)
    {
        this.bookId = bookId;
    }
    public String getDescription() {
        return description;
    }

    public String getCoverUrl()
    {
        return coverUrl;
    }

    public String getAuthor()
    {
        return author;
    }

    public String getTitle()
    {
        return title;
    }

    public String getRating() {
        return rating;
    }

}
package org.example;

class MovieDetails {
    private String title;
    private String poster;
    private String plot;
    private String summary;
    private String omdbid;

    public MovieDetails(String title, String poster, String plot, String summary, String omdbid) {
        this.title = title;
        this.poster = poster;
        this.plot = plot;
        this.summary = summary;
        this.omdbid = omdbid;
    }

    public void setOmdbid(String omdbid) {
        this.omdbid = omdbid;
    }

    public String getOmdbid() {
        return omdbid;
    }
    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getSummary() {
        return summary;
    }

    public String getTitle() {
        return title;
    }

    public String getPoster() {
        return poster;
    }

    public String getPlot() {
        return plot;
    }
}
package org.example;

import javax.swing.*;

// Создаем класс с методом main
public class Main {
    public static void main(String[] args) {
        try {
            SwingUtilities.invokeLater(() -> new CollectionManager());
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Ошибка при запуске приложения: " + e.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }
}
