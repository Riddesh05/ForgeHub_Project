document.addEventListener("DOMContentLoaded", function () {

    const email = sessionStorage.getItem("email");

    if (!email) {
        window.location.href = "/";
        return;
    }

    sendRecoveryOtp(email);
});


async function sendRecoveryOtp(email) {

    const message = document.getElementById("message");

    try {

        const response = await fetch(
            "/auth/2fa/recovery/send-otp?email=" +
            encodeURIComponent(email),
            {
                method: "POST"
            }
        );

        const data = await response.text();

        if (!response.ok) {

            message.textContent =
                data || "Unable to send recovery OTP.";

            message.className = "alert alert-danger";

            return;
        }

        message.textContent =
            "A verification code has been sent to your registered email.";

        message.className = "alert alert-success";

    } catch (error) {

        message.textContent =
            "Unable to connect to the server.";

        message.className = "alert alert-danger";
    }
}


async function verifyEmailOtp() {

    const email = sessionStorage.getItem("email");
    const emailOtp =
        document.getElementById("emailOtp").value.trim();

    const message = document.getElementById("message");

    const button =
        document.getElementById("verifyEmailButton");

    const buttonText =
        document.getElementById("verifyEmailText");

    const spinner =
        document.getElementById("verifyEmailSpinner");

    message.className = "alert d-none";

    if (!email) {

        window.location.href = "/";
        return;
    }

    if (!emailOtp || emailOtp.length !== 6) {

        message.textContent =
            "Please enter the 6-digit email OTP.";

        message.className = "alert alert-danger";

        return;
    }

    button.disabled = true;
    buttonText.textContent = "Verifying...";
    spinner.classList.remove("d-none");

    try {

        const response = await fetch(
            "/auth/2fa/recovery",
            {
                method: "POST",

                headers: {
                    "Content-Type": "application/json"
                },

                body: JSON.stringify({
                    email: email,
                    emailOtp: emailOtp
                })
            }
        );

        const data = await response.json();

        if (!response.ok) {

            message.textContent =
                data.message || "Invalid email OTP.";

            message.className = "alert alert-danger";

            return;
        }

        if (data.requiresTwoFactorSetup && data.qrCode) {

            sessionStorage.setItem(
                "qrCode",
                data.qrCode
            );

            window.location.href = "/setup-2fa";

            return;
        }

        message.textContent =
            "Unexpected response from server.";

        message.className = "alert alert-danger";

    } catch (error) {

        message.textContent =
            "Unable to connect to the server.";

        message.className = "alert alert-danger";

    } finally {

        button.disabled = false;
        buttonText.textContent = "Verify email";
        spinner.classList.add("d-none");
    }
}