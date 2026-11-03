import GoogleSignIn
import Shared
import UIKit

enum GoogleSignInBridge {
    static func configure() {
        IosGoogleSignInBridge.shared.configure(
            signIn: { completion in
                signIn(completion: completion)
            },
            signOut: {
                GIDSignIn.sharedInstance.signOut()
            }
        )
    }

    private static func signIn(completion: IosGoogleSignInCompletion) {
        guard let presentingViewController = UIApplication.shared.presentingViewController else {
            completion.onFailure()
            return
        }

        GIDSignIn.sharedInstance.signIn(withPresenting: presentingViewController) { result, error in
            if let error {
                if (error as NSError).code == GIDSignInErrorCode.canceled.rawValue {
                    completion.onCancelled()
                } else {
                    completion.onFailure()
                }
                return
            }

            guard
                let user = result?.user,
                let idToken = user.idToken?.tokenString,
                let subject = user.userID,
                let profile = user.profile
            else {
                completion.onFailure()
                return
            }

            completion.onSuccess(
                idToken: idToken,
                subject: subject,
                email: profile.email,
                displayName: profile.name
            )
        }
    }
}

private extension UIApplication {
    var presentingViewController: UIViewController? {
        let activeScene = connectedScenes
            .compactMap { $0 as? UIWindowScene }
            .first { $0.activationState == .foregroundActive }
        var controller = activeScene?.windows
            .first { $0.isKeyWindow }?
            .rootViewController

        while let presented = controller?.presentedViewController {
            controller = presented
        }
        return controller
    }
}
