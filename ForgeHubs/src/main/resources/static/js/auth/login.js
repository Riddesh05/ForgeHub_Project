async function login() {

    const email = document.getElementById("email").value.trim();
    const password = document.getElementById("password").value;

    const errorMessage = document.getElementById("errorMessage");
    const loginButton = document.getElementById("loginButton");
    const loginText = document.getElementById("loginText");
    const loginSpinner = document.getElementById("loginSpinner");

    errorMessage.classList.add("d-none");

    if (!email || !password) {

        errorMessage.textContent =
            "Please enter email and password.";

        errorMessage.classList.remove("d-none");

        return;
    }

    loginButton.disabled = true;
    loginText.textContent = "Logging in...";
    loginSpinner.classList.remove("d-none");

    try {

        const response = await fetch("/auth/login", {

            method: "POST",

            headers: {
                "Content-Type": "application/json"
            },

            body: JSON.stringify({
                email: email,
                password: password
            })
        });

        const data = await response.json();

        if (!response.ok) {

            errorMessage.textContent =
                data.message || "Invalid email or password.";

            errorMessage.classList.remove("d-none");

            return;
        }

        if (data.requiresTwoFactorSetup) {

            sessionStorage.setItem("email", email);
            sessionStorage.setItem("qrCode", data.qrCode);

            window.location.href = "/setup-2fa";

            return;
        }

        if (data.requiresTwoFactor) {

            sessionStorage.setItem("email", email);

            window.location.href = "/verify-2fa";

            return;
        }

        if (data.accessToken) {

            sessionStorage.setItem(
                "accessToken",
                data.accessToken
            );

            sessionStorage.setItem(
                "refreshToken",
                data.refreshToken
            );
            sessionStorage.setItem("role", data.role);
            sessionStorage.setItem("userId", data.userId);
            sessionStorage.setItem("name", data.name);
            sessionStorage.setItem("email", data.email);

            if (data.role === "ADMIN") {
                window.location.href = "/admin/dashboard";
            } else if (data.role === "VENDOR") {
                window.location.href = "/vendor/dashboard?vendorId=" + encodeURIComponent(data.userId);
            } else {
                window.location.href = "/dashboard";
            }

            return;
        }

        errorMessage.textContent =
            "Unexpected response from server.";

        errorMessage.classList.remove("d-none");

    } catch (error) {

        errorMessage.textContent =
            "Unable to connect to the server.";

        errorMessage.classList.remove("d-none");

    } finally {

        loginButton.disabled = false;
        loginText.textContent = "Login";
        loginSpinner.classList.add("d-none");
    }
}