const optionInputs = [...document.querySelectorAll("[data-option]")];
const counter = document.getElementById("counter");
const totalPrice = document.getElementById("totalPrice");
const serviceList = document.getElementById("serviceList");
const message = document.getElementById("message");
const activateButton = document.getElementById("activateButton");
const resetButton = document.getElementById("resetButton");
const passType = document.getElementById("passType");
const passDescription = document.getElementById("passDescription");
const presetButtons = document.getElementById("presetButtons");
const presetMessage = document.getElementById("presetMessage");
let activePreset = null;
let currentPass = null;
let requestVersion = 0;

/** Fetches JSON and reports HTTP errors before any pass is rendered. */
async function fetchJson(url) {
    const response = await fetch(url);
    const data = await response.json();
    if (!response.ok) {
        throw new Error(data.error || "The museum service could not complete the request.");
    }
    return data;
}

/** Renders prices, services, and selected options supplied by the server. */
function renderPass(pass) {
    totalPrice.textContent = `$${pass.price.toFixed(2)}`;
    passDescription.textContent = pass.description;
    counter.textContent = `${pass.options.length} ADD-ON${pass.options.length === 1 ? "" : "S"}`;
    serviceList.replaceChildren();
    for (const service of pass.services) {
        const item = document.createElement("span");
        item.className = "service";
        item.textContent = service;
        serviceList.appendChild(item);
    }
    for (const input of optionInputs) {
        input.checked = pass.options.includes(input.dataset.option);
    }
    for (const button of presetButtons.children) {
        button.setAttribute("aria-pressed", String(button.dataset.preset === activePreset));
    }
}

/** Requests the selected factory product and preset or custom builder options. */
async function updatePass() {
    const version = ++requestVersion;
    const params = new URLSearchParams({ type: passType.value });
    if (activePreset !== null) {
        params.set("preset", activePreset);
    } else {
        const options = [];
        for (const input of optionInputs) {
            if (input.checked) options.push(input.dataset.option);
        }
        params.set("options", options.join(","));
    }
    currentPass = null;
    activateButton.disabled = true;
    totalPrice.textContent = "Loading...";
    serviceList.replaceChildren();
    message.textContent = "";
    try {
        const pass = await fetchJson(`/api/pass?${params}`);
        if (version !== requestVersion) return;
        renderPass(pass);
        currentPass = pass;
        activateButton.disabled = false;
    } catch (error) {
        if (version !== requestVersion) return;
        totalPrice.textContent = "Unavailable";
        message.textContent = error.message || "Unable to connect to the museum service.";
    }
}

/** Switches from a preset to the user's manually selected options. */
function customizePass() {
    activePreset = null;
    updatePass();
}

/** Loads the selected preset through the pass API for the current pass type. */
function selectPreset(event) {
    activePreset = event.currentTarget.dataset.preset;
    updatePass();
}

/** Discovers preset names from the registry API and creates their controls. */
async function loadPresets() {
    try {
        const names = await fetchJson("/api/presets");
        for (const name of names) {
            const button = document.createElement("button");
            button.type = "button";
            button.className = "nav-button";
            button.dataset.preset = name;
            button.textContent = name.charAt(0).toUpperCase() + name.slice(1);
            button.setAttribute("aria-pressed", "false");
            button.addEventListener("click", selectPreset);
            presetButtons.appendChild(button);
        }
    } catch (error) {
        presetMessage.textContent = "Presets are unavailable. You can still select experiences individually.";
    }
}

/** Shows the API activation result belonging to the currently displayed pass. */
function activatePass() {
    if (currentPass !== null) message.textContent = currentPass.activation;
}

/** Clears optional experiences while retaining the selected base pass type. */
function resetPass() {
    activePreset = null;
    for (const input of optionInputs) input.checked = false;
    updatePass();
}

for (const input of optionInputs) input.addEventListener("change", customizePass);
passType.addEventListener("change", updatePass);
activateButton.addEventListener("click", activatePass);
resetButton.addEventListener("click", resetPass);
loadPresets();
updatePass();
