# Email Spam Classifier

A simple web-based email spam classifier powered by Java, HTML, CSS, and JavaScript.

## Project structure

- `src/EmailSpamClassifier.java` – Java backend server and classifier logic.
- `public/index.html` – Frontend page.
- `public/style.css` – Styling and gradient theme.
- `public/script.js` – Browser interaction and backend API calls.

## Requirements

- Java JDK 17 or newer
- A modern browser: Chrome, Firefox, or Edge

## Run locally

1. Open a terminal in the project root.
2. Compile the Java backend:

```powershell
javac -d out src\EmailSpamClassifier.java
```

3. Start the server:

```powershell
java -cp out EmailSpamClassifier
```

4. Open your browser and go to:

```text
http://localhost:8080
```

## How to use

- Enter an email subject and body.
- Click **Classify Message**.
- The page shows whether the message is likely spam and a detection score.

## Open in Chrome, Firefox, or Edge

- Chrome: open `http://localhost:8080`
- Firefox: open `http://localhost:8080`
- Edge: open `http://localhost:8080`

If you want, you can also use:

```powershell
start chrome http://localhost:8080
start firefox http://localhost:8080
start msedge http://localhost:8080
```

## Notes

- This project demonstrates a rule-based spam detection model. It is not a production classifier but is useful for learning and GitHub portfolio use.
- The backend server serves the static HTML/CSS/JS assets and processes classification requests.
