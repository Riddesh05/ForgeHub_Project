document.addEventListener("DOMContentLoaded", function () {

    const email = sessionStorage.getItem("email");

    if (!email) {
        window.location.href = "/";
    }

});

async function verifyOtp() {

    const email = sessionStorage.getItem("email");
    const otp = document.getElementById("otp").value.trim();

    const errorMessage = document.getElementById("errorMessage");
    const verifyButton = document.getElementById("verifyButton");
    const verifyText = document.getElementById("verifyText");
    const verifySpinner = document.getElementById("verifySpinner");

    errorMessage.classList.add("d-none");

    if (!otp || otp.length !== 6) {

        errorMessage.textContent =
            "Please enter the 6-digit authenticator code.";

        errorMessage.classList.remove("d-none");

        return;
    }

    verifyButton.disabled = true;
    verifyText.textContent = "Verifying...";
    verifySpinner.classList.remove("d-none");

    try {

        const response = await fetch("/auth/login", {

            method: "POST",

            headers: {
                "Content-Type": "application/json"
            },

            body: JSON.stringify({
                email: email,
                authenticatorOtp: otp
            })
        });

        const data = await response.json();

        if (!response.ok) {

            errorMessage.textContent =
                data.message || "Invalid authenticator code.";

            errorMessage.classList.remove("d-none");

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

            sessionStorage.removeItem("email");
            sessionStorage.setItem("role", data.role);
            sessionStorage.setItem("userId", data.userId);

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
            "Authentication failed.";

        errorMessage.classList.remove("d-none");

    } catch (error) {

        errorMessage.textContent =
            "Unable to connect to the server.";

        errorMessage.classList.remove("d-none");

    } finally {

        verifyButton.disabled = false;
        verifyText.textContent = "Verify and continue";
        verifySpinner.classList.add("d-none");
    }
}