import XCTest

/// Drives the installed application's normal UI; the host captures its actual framebuffer.
final class StoreCapture: XCTestCase {
  private var app: XCUIApplication!
  private var labels: [String: String] = [:]
  private var isDuoInner = false

  private var contentRoot: XCUIElement {
    guard isDuoInner else { return app }
    let windows = app.windows.allElementsBoundByIndex.filter {
      $0.frame.width > 600 && $0.frame.width > $0.frame.height
        && $0.keyboards.count == 0
    }
    guard let window = windows.first else {
      XCTFail("Missing unfolded application window: \(app.debugDescription)")
      return app
    }
    return window
  }

  func testCaptureDeck() throws {
    continueAfterFailure = false
    let config =
      try JSONSerialization.jsonObject(with: Data(captureConfigJSON.utf8)) as! [String: Any]
    labels = config["labels"] as! [String: String]
    isDuoInner = config["profile"] as? String == "duo-inner"
    app = XCUIApplication(bundleIdentifier: config["bundle"] as! String)
    app.launchArguments = [
      "-AppleLanguages", "(\(config["language"] as! String))", "-AppleLocale",
      config["locale"] as! String,
    ]
    app.launch()
    Thread.sleep(forTimeInterval: 2)
    if config["section"] as? String == "appearance" {
      try captureAppearance()
      return
    }
    if config["prepare"] as? String != "existing" { try prepareDemo() }
    try tab("txt2img")
    // The unfolded capture uses a blank Demo prompt so its floating keyboard
    // cannot cover the controls needed to capture the rest of the deck.
    if config["prepare"] as? String != "existing" && !isDuoInner {
      try tap("prompt")
      app.typeText(config["prompt"] as! String)
      contentRoot.coordinate(withNormalizedOffset: CGVector(dx: 0.01, dy: 0.18)).tap()
      Thread.sleep(forTimeInterval: 0.7)
    }
    try tap("showAdvanced", isOptional: true)
    try tap("generate")
    XCTAssertTrue(element("close").waitForExistence(timeout: 90))
    try save("01-create")
    try tap("close")
    try save("02-controls")
    for _ in 0..<5 {
      try tap("generate")
      XCTAssertTrue(element("close").waitForExistence(timeout: 90))
      try tap("close")
    }
    try tab("gallery")
    try save("04-gallery")
    let image = contentRoot.buttons.allElementsBoundByIndex.first {
      $0.isHittable && $0.frame.width > 70 && $0.frame.height > 70
        && abs($0.frame.width - $0.frame.height) < 4
    }
    XCTAssertNotNil(image, "Gallery must contain a real generated sample")
    image!.tap()
    try tap("galleryInfo")
    try save("06-open")
    try tap("back")
    try tab("settings")
    try tap("configuration")
    XCTAssertTrue(element("next").waitForExistence(timeout: 15))
    try save("03-providers")
    try tap("back")
    try captureAppearance(shouldNavigate: false)
  }

  /// Captures one real Settings layout without mixing independently laid-out themes.
  private func captureAppearance(shouldNavigate: Bool = true) throws {
    if shouldNavigate { try tab("settings") }
    try scrollTo("accentColor")
    try save("settings-light")
    try save("05-settings")
  }

  private func element(_ key: String) -> XCUIElement {
    let label = labels[key] ?? key
    let root = contentRoot
    let matchingButtons = root.buttons.matching(NSPredicate(format: "label == %@", label))
    if let visible = matchingButtons.allElementsBoundByIndex.first(where: { $0.isHittable }) {
      return visible
    }
    let matches = root.descendants(matching: .any).matching(
      NSPredicate(format: "label == %@", label))
    if let visible = matches.allElementsBoundByIndex.first(where: { $0.isHittable }) {
      return visible
    }
    return matchingButtons.firstMatch.exists ? matchingButtons.firstMatch : matches.firstMatch
  }

  private func tap(_ key: String, isOptional: Bool = false) throws {
    let target = element(key)
    if isOptional && !target.exists { return }
    XCTAssertTrue(target.waitForExistence(timeout: 12), "Missing \(key): \(app.debugDescription)")
    if !target.isHittable { try scrollTo(key) }
    XCTAssertTrue(target.isHittable, "Not hittable: \(key)")
    target.tap()
    Thread.sleep(forTimeInterval: 0.6)
  }

  private func scrollTo(_ key: String) throws {
    for _ in 0..<8 {
      if element(key).isHittable {
        Thread.sleep(forTimeInterval: 0.7)
        return
      }
      contentRoot.swipeUp()
      Thread.sleep(forTimeInterval: 0.3)
    }
    XCTAssertTrue(element(key).isHittable, "Cannot reveal \(key): \(app.debugDescription)")
  }

  private func tab(_ key: String) throws {
    for _ in 0..<3 {
      if element(key).isHittable { break }
      let next = contentRoot.buttons["Next Page"].firstMatch
      if next.isHittable { next.tap() }
      Thread.sleep(forTimeInterval: 0.4)
    }
    try tap(key)
  }

  private func prepareDemo() throws {
    // Select the footer control; tutorial illustrations also contain Next controls.
    for _ in 0..<7 {
      if element("startFresh").isHittable || element("connect").isHittable { break }
      let root = contentRoot
      let candidates = root.descendants(matching: .any).matching(
        NSPredicate(format: "label == %@", labels["next"]!)
      ).allElementsBoundByIndex.filter { $0.isHittable }
      if let next = candidates.max(by: { $0.frame.maxY < $1.frame.maxY }) {
        let frame = next.frame
        root.coordinate(
          withNormalizedOffset: CGVector(
            dx: (frame.midX - root.frame.minX) / root.frame.width,
            dy: (frame.midY - root.frame.minY) / root.frame.height)
        ).tap()
        Thread.sleep(forTimeInterval: 1)
      }
    }
    try tap("startFresh", isOptional: true)
    // The normal setup defaults to AUTOMATIC1111.
    try tap("next")
    try scrollTo("demoMode")
    try tap("demoMode")
    try tap("connect")
    if element("demoTitle").waitForExistence(timeout: 5) {
      try scrollTo("demoUnderstand")
      try tap("demoUnderstand")
      try scrollTo("demoFeedback")
      try tap("demoFeedback")
      try tap("demoContinue")
    }
    XCTAssertTrue(element("txt2img").waitForExistence(timeout: 45), app.debugDescription)
  }

  private func save(_ name: String) throws {
    Thread.sleep(forTimeInterval: 1)
    let marker = URL(fileURLWithPath: NSTemporaryDirectory()).appendingPathComponent(
      "sdai-store-capture.ready")
    try Data(name.utf8).write(to: marker, options: .atomic)
    let deadline = Date().addingTimeInterval(30)
    while FileManager.default.fileExists(atPath: marker.path), Date() < deadline {
      Thread.sleep(forTimeInterval: 0.15)
    }
    XCTAssertFalse(
      FileManager.default.fileExists(atPath: marker.path), "Host did not capture \(name)")
  }
}
