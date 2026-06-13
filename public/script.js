const form = document.getElementById('classifier-form');
const resultPanel = document.getElementById('result-panel');

form.addEventListener('submit', async (event) => {
    event.preventDefault();
    const subject = document.getElementById('subject').value.trim();
    const body = document.getElementById('body').value.trim();

    if (!subject && !body) {
        resultPanel.innerHTML = '<p class="hint">Please enter a subject or email body to classify.</p>';
        return;
    }

    resultPanel.innerHTML = '<p class="hint">Analyzing message content...</p>';

    try {
        const response = await fetch('/api/classify', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ subject, body })
        });

        if (!response.ok) {
            throw new Error('Server returned ' + response.status);
        }

        const data = await response.json();
        const labelClass = data.label === 'Spam' ? 'spam' : 'ham';

        resultPanel.innerHTML = `
            <div class="meta"><strong>Subject</strong>: ${escapeHtml(data.subject || '(empty subject)')}</div>
            <div class="meta"><strong>Score</strong>: ${data.score}</div>
            <div class="status ${labelClass}">${data.label}</div>
            <p>${escapeHtml(data.explanation)}</p>
        `;
    } catch (error) {
        resultPanel.innerHTML = `<p class="hint">Unable to classify message: ${escapeHtml(error.message)}</p>`;
    }
});

function escapeHtml(text) {
    return text
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#039;');
}
