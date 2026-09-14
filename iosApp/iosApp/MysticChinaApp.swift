import SwiftUI
import UIKit

// ═══════════════════════════════════════════════════════════
// 沉浸式容器 — 将 Kuikly 渲染视图延伸到状态栏区域
// ═══════════════════════════════════════════════════════════
class ImmersiveContainerVC: UIViewController {

    private var kuiklyVC: UIViewController?
    private var isKuiklyEmbedded = false
    private let kuiklyPageName = "MainPage"

    override func viewDidLoad() {
        super.viewDidLoad()
        view.backgroundColor = .clear
    }

    // 属性覆盖 — 必须在 viewDidLoad 之前生效
    override var edgesForExtendedLayout: UIRectEdge { .all }
    override var extendedLayoutIncludesOpaqueBars: Bool { true }
    override var preferredStatusBarStyle: UIStatusBarStyle { .lightContent }

    override func viewDidLayoutSubviews() {
        super.viewDidLayoutSubviews()
        if isKuiklyEmbedded, let vc = kuiklyVC {
            // 每次布局时更新 Kuikly 视图帧，确保覆盖整个屏幕（含状态栏区域）
            vc.view.frame = view.bounds
        }
    }

    func embedKuiklyPager() {
        guard let vc = KuiklyPagerManager.shared.startPager(pageName: kuiklyPageName) else { return }
        kuiklyVC = vc

        addChild(vc)
        vc.view.autoresizingMask = [.flexibleWidth, .flexibleHeight]
        vc.view.frame = view.bounds  // 初始帧，viewDidLayoutSubviews 会修正
        view.addSubview(vc.view)
        vc.didMove(toParent: self)
        isKuiklyEmbedded = true
    }
}

// ═══════════════════════════════════════════════════════════
// SwiftUI 入口
// ═══════════════════════════════════════════════════════════
@main
struct MysticChinaApp: App {
    var body: some Scene {
        WindowGroup {
            KuiklyViewController()
                .ignoresSafeArea(.all)
        }
    }
}

struct KuiklyViewController: UIViewControllerRepresentable {

    func makeUIViewController(context: Context) -> UIViewController {
        let container = ImmersiveContainerVC()
        container.embedKuiklyPager()
        return container
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {
    }
}
