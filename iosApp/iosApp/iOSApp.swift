import SwiftUI
import GoogleSignIn

@main
struct iOSApp: App {
    init() {
        GoogleSignInBridge.configure()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
                .onOpenURL { url in
                    GIDSignIn.sharedInstance.handle(url)
                }
        }
    }
}
