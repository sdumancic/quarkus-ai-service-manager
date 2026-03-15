const chatWindow = document.getElementById('chat-window');
const messageInput = document.getElementById('message-input');
const sendBtn = document.getElementById('send-btn');
const uuidInput = document.getElementById('customer-uuid');

function appendMessage(role, text) {
    const msgDiv = document.createElement('div');
    msgDiv.className = `message ${role}-message`;
    
    if (role === 'agent' && typeof marked !== 'undefined') {
        msgDiv.innerHTML = marked.parse(text);
    } else {
        msgDiv.textContent = text;
    }

    chatWindow.appendChild(msgDiv);
    chatWindow.scrollTop = chatWindow.scrollHeight;
    return msgDiv;
}

function showTyping() {
    const typingDiv = document.createElement('div');
    typingDiv.className = 'message agent-message typing';
    typingDiv.id = 'typing-indicator';
    typingDiv.textContent = 'Agent is thinking...';
    chatWindow.appendChild(typingDiv);
    chatWindow.scrollTop = chatWindow.scrollHeight;
}

function hideTyping() {
    const indicator = document.getElementById('typing-indicator');
    if (indicator) indicator.remove();
}

async function sendMessage() {
    const message = messageInput.value.trim();
    const customerUuid = uuidInput.value.trim();

    if (!message || !customerUuid) return;

    appendMessage('user', message);
    messageInput.value = '';
    messageInput.focus();
    
    showTyping();
    sendBtn.disabled = true;

    try {
        const response = await fetch('/agent/run', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'text/event-stream'
            },
            body: JSON.stringify({
                message: message,
                customerUuid: customerUuid
            })
        });

        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        const reader = response.body.getReader();
        const decoder = new TextDecoder();
        let agentMsgDiv = null;

        while (true) {
            const { value, done } = await reader.read();
            if (done) break;

            const chunk = decoder.decode(value, { stream: true });
            const lines = chunk.split('\n');

            for (const line of lines) {
                if (line.startsWith('data:')) {
                    try {
                        const eventData = JSON.parse(line.substring(5));
                        console.log('Received event:', eventData);

                        if (eventData.type === 'TEXT_MESSAGE_CONTENT') {
                            hideTyping();
                            if (agentMsgDiv) {
                                agentMsgDiv.innerHTML = marked.parse(eventData.content);
                            } else {
                                agentMsgDiv = appendMessage('agent', eventData.content);
                            }
                        }
                    } catch (e) {
                        console.error('Error parsing event data:', e, 'Line:', line);
                    }
                }
            }
        }

    } catch (error) {
        console.error('Error:', error);
        hideTyping();
        appendMessage('agent', 'Sorry, I encountered an error connecting to the service.');
    } finally {
        hideTyping();
        sendBtn.disabled = false;
    }
}

sendBtn.addEventListener('click', sendMessage);

messageInput.addEventListener('keypress', (e) => {
    if (e.key === 'Enter') {
        sendMessage();
    }
});

// Focus input on load
window.onload = () => messageInput.focus();
