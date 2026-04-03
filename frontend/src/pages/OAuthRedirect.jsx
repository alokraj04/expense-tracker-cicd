import { useEffect } from "react";

export default function OAuthRedirect() {
    useEffect(() => {
        const params = new URLSearchParams(window.location.search);

        const token = params.get("token");
        const userId = params.get("userId");

        console.log("TOKEN:", token);

        if (token) {
            localStorage.setItem("accessToken", token);
            localStorage.setItem("token", token); // Store explicitly securely

            if (userId) {
                localStorage.setItem("userId", userId);
            }

            console.log("LOCAL STORAGE:", localStorage.getItem("token"));

            // ✅ Hard redirect to refresh auth state mapping
            window.location.href = "/dashboard";
        } else {
            window.location.href = "/login";
        }
    }, []);

    return (
        <div className="flex items-center justify-center h-screen text-white">
            Logging you in...
        </div>
    );
}