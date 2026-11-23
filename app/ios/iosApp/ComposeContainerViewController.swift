import UIKit

/// Hosts Compose and preserves its native materials in the system's background snapshot.
/// The full window is captured while active because isolated effect views cannot capture their backdrop.
final class ComposeContainerViewController: UIViewController {
    private let contentController: UIViewController
    private var backgroundImage: UIImage?
    private var snapshotView: UIImageView?

    /// Embeds the shared app controller without introducing another safe-area inset.
    /// - Parameter contentController: Compose controller retained for this host's lifetime.
    init(contentController: UIViewController) {
        self.contentController = contentController
        super.init(nibName: nil, bundle: nil)
    }

    @available(*, unavailable)
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    override func viewDidLoad() {
        super.viewDidLoad()
        addChild(contentController)
        view.backgroundColor = .systemBackground
        let contentView = contentController.view!
        contentView.translatesAutoresizingMaskIntoConstraints = false
        view.addSubview(contentView)
        NSLayoutConstraint.activate([
            contentView.leadingAnchor.constraint(equalTo: view.leadingAnchor),
            contentView.trailingAnchor.constraint(equalTo: view.trailingAnchor),
            contentView.topAnchor.constraint(equalTo: view.topAnchor),
            contentView.bottomAnchor.constraint(equalTo: view.bottomAnchor),
        ])
        contentController.didMove(toParent: self)

        let center = NotificationCenter.default
        center.addObserver(
            self, selector: #selector(sceneWillDeactivate(_:)), name: UIScene.willDeactivateNotification, object: nil)
        center.addObserver(
            self, selector: #selector(sceneDidEnterBackground(_:)), name: UIScene.didEnterBackgroundNotification,
            object: nil)
        center.addObserver(
            self, selector: #selector(sceneDidActivate(_:)), name: UIScene.didActivateNotification, object: nil)
    }

    deinit {
        NotificationCenter.default.removeObserver(self)
        snapshotView?.removeFromSuperview()
    }

    /// Captures the entire window before UIKit suspends native effect rendering.
    /// - Parameter notification: Scene transition; other windows do not affect this controller.
    @objc private func sceneWillDeactivate(_ notification: Notification) {
        guard let window = viewIfLoaded?.window,
            let scene = notification.object as? UIScene,
            scene === window.windowScene,
            !window.bounds.isEmpty
        else { return }
        let renderer = UIGraphicsImageRenderer(bounds: window.bounds)
        var hasDrawnHierarchy = false
        let image = renderer.image { _ in
            hasDrawnHierarchy = window.drawHierarchy(in: window.bounds, afterScreenUpdates: false)
        }
        backgroundImage = hasDrawnHierarchy ? image : nil
    }

    /// Supplies the active appearance only while the system takes its app-switcher snapshot.
    /// - Parameter notification: Background transition for the owning scene.
    @objc private func sceneDidEnterBackground(_ notification: Notification) {
        guard let window = viewIfLoaded?.window,
            let scene = notification.object as? UIScene,
            scene === window.windowScene,
            let image = backgroundImage
        else { return }
        snapshotView?.removeFromSuperview()
        let overlay = UIImageView(image: image)
        overlay.frame = window.bounds
        overlay.autoresizingMask = [.flexibleWidth, .flexibleHeight]
        overlay.isUserInteractionEnabled = false
        overlay.accessibilityElementsHidden = true
        window.addSubview(overlay)
        snapshotView = overlay
        backgroundImage = nil
    }

    /// Restores the live hierarchy and releases the in-memory snapshot on every foreground return.
    /// - Parameter notification: Activation for the owning scene, including cancelled app switching.
    @objc private func sceneDidActivate(_ notification: Notification) {
        guard let scene = notification.object as? UIScene,
            scene === viewIfLoaded?.window?.windowScene
        else { return }
        snapshotView?.removeFromSuperview()
        snapshotView = nil
        backgroundImage = nil
    }
}
