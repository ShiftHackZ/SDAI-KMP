import AiSdPresentation
import SwiftUI

@main
struct AIStableDiffusionIOSApp: App {
  init() {
    let koin = KoinBootstrapKt.doInitKoin()
    IosWorkExecutionModuleKt.installIosWorkExecution(
      koin: koin, execution: IosWorkExecutionAdapter())
    SiliconDiffusionMnnRuntimeComposition.install(koin: koin)
    OpenAiOAuthModuleKt.registerOpenAiOAuthFeature()
    SiliconDiffusionBonsaiRuntimeRegistration.registerIfAvailable()
    SiliconDiffusionCoreMLRuntimeRegistration.registerIfAvailable()
  }

  var body: some Scene {
    WindowGroup {
      ComposeView()
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(Color(uiColor: .systemBackground).ignoresSafeArea())
        .ignoresSafeArea()
    }
  }
}
