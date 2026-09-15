import SwiftUI
import UIKit
import OpenKuiklyIOSRender

// ═══════════════════════════════════════════════════════════
// 沉浸式容器 — 将 Kuikly 渲染视图延伸到状态栏区域
// ═══════════════════════════════════════════════════════════
class ImmersiveContainerVC: UIViewController, KuiklyViewBaseDelegate {

    private var kuiklyView: KuiklyBaseView?
    private let kuiklyPageName = "MainPage"

    override func viewDidLoad() {
        super.viewDidLoad()
        view.backgroundColor = .clear
    }

    // 布局属性在首次加载视图前由 makeUIViewController 设置
    override var preferredStatusBarStyle: UIStatusBarStyle { .default }

    override func viewDidLayoutSubviews() {
        super.viewDidLayoutSubviews()
        if let kuiklyView {
            kuiklyView.frame = view.bounds
        } else if view.bounds.width > 0 && view.bounds.height > 0 {
            let renderView = KuiklyBaseView(frame: view.bounds, pageName: kuiklyPageName,
                                            pageData: [:], delegate: self, frameworkName: "shared")
            renderView.autoresizingMask = [.flexibleWidth, .flexibleHeight]
            view.addSubview(renderView)
            kuiklyView = renderView
        }
    }

    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        kuiklyView?.viewWillAppear()
    }

    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)
        kuiklyView?.viewDidAppear()
    }

    override func viewWillDisappear(_ animated: Bool) {
        kuiklyView?.viewWillDisappear()
        super.viewWillDisappear(animated)
    }

    override func viewDidDisappear(_ animated: Bool) {
        kuiklyView?.viewDidDisappear()
        super.viewDidDisappear(animated)
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
        container.edgesForExtendedLayout = .all
        container.extendedLayoutIncludesOpaqueBars = true
        return container
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {
    }
}
