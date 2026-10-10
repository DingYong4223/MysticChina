import SwiftUI
import UIKit
import shared
import WebKit
import OpenKuiklyIOSRender

private final class MysticChinaImageLoader: NSObject, KuiklyRenderComponentExpandProtocol {
    func hr_setImage(withUrl url: String?, for imageView: UIImageView) -> Bool {
        if url == nil { imageView.image = nil; return true }
        return false // The callback-based method below handles nonempty URLs.
    }

    @objc(hr_setImageWithUrl:imageParams:complete:)
    func hr_setImage(withUrl loadURL: String?, imageParams: [AnyHashable: Any]?,
                     complete: @escaping ImageCompletionBlock) -> Bool {
        guard let loadURL else { complete(nil, nil, nil); return true }
        guard let url = URL(string: loadURL),
              ["https", "http", "file"].contains(url.scheme?.lowercased() ?? "") else {
            complete(nil, NSError(domain: "MysticChinaImage", code: -1), URL(string: loadURL))
            return true
        }
        if url.isFileURL {
            let image = UIImage(contentsOfFile: url.path)
            complete(image, image == nil ? NSError(domain: "MysticChinaImage", code: -2) : nil, url)
            return true
        }
        URLSession.shared.dataTask(with: url) { data, _, error in
            let image = data.flatMap(UIImage.init(data:))
            complete(image, error ?? (image == nil ? NSError(domain: "MysticChinaImage", code: -3) : nil), url)
        }.resume()
        return true
    }
}

// ═══════════════════════════════════════════════════════════
// 沉浸式容器 — 将 Kuikly 渲染视图延伸到状态栏区域
// ═══════════════════════════════════════════════════════════
class ImmersiveContainerVC: UIViewController, KuiklyViewBaseDelegate, KRRouterProtocol {

    private var kuiklyView: KuiklyBaseView?
    private var kuiklyPageName = "HomePage"
    private var kuiklyPageData: [AnyHashable: Any] = [:]

    override func viewDidLoad() {
        super.viewDidLoad()
        view.backgroundColor = .clear
    }

    // 布局属性在首次加载视图前由 makeUIViewController 设置
    override var preferredStatusBarStyle: UIStatusBarStyle { .default }

    override func viewDidLayoutSubviews() {
        super.viewDidLayoutSubviews()
        let contentFrame = view.bounds
        if let kuiklyView {
            kuiklyView.frame = contentFrame
        } else if contentFrame.width > 0 && contentFrame.height > 0 {
            let renderView = KuiklyBaseView(frame: contentFrame, pageName: kuiklyPageName,
                                            pageData: kuiklyPageData, delegate: self, frameworkName: "shared")
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

    func openPage(withName pageName: String, pageData: [AnyHashable: Any]?, controller: UIViewController) {
        let next = ImmersiveContainerVC()
        next.kuiklyPageName = App.shared.resolvePageName(pageName: pageName, pageID: pageData?["pageID"] as? String ?? "")
        next.kuiklyPageData = pageData ?? [:]
        next.edgesForExtendedLayout = .all
        next.extendedLayoutIncludesOpaqueBars = true
        controller.navigationController?.pushViewController(next, animated: true)
    }

    func closePage(_ controller: UIViewController) {
        if let navigation = controller.navigationController, navigation.viewControllers.count > 1 {
            navigation.popViewController(animated: true)
        }
    }
}

// ═══════════════════════════════════════════════════════════
// SwiftUI 入口
// ═══════════════════════════════════════════════════════════
@main
struct MysticChinaApp: SwiftUI.App {
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
        KRRouterModule.registerRouterHandler(container)
        KuiklyRenderBridge.registerComponentExpandHandler(MysticChinaImageLoader())
        let navigation = UINavigationController(rootViewController: container)
        navigation.setNavigationBarHidden(true, animated: false)
        return navigation
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {
    }
}

@objc(HanziWebView)
final class HanziWebView: UIView, KuiklyRenderViewExportProtocol {
    private let webView = WKWebView(frame: .zero)

    override init(frame: CGRect) {
        super.init(frame: frame)
        webView.isOpaque = false
        webView.backgroundColor = .clear
        addSubview(webView)
    }

    required init?(coder: NSCoder) { fatalError("init(coder:) has not been implemented") }

    override func layoutSubviews() {
        super.layoutSubviews()
        webView.frame = bounds
    }

    func hrv_setProp(withKey propKey: String, propValue: Any) {
        if propKey != "src" {
            _ = css_setProp(withKey: propKey, value: propValue)
            return
        }
        guard let filename = propValue as? String,
              !filename.contains("/"), !filename.contains(".."),
              let root = Bundle.main.resourceURL?.appendingPathComponent("hanzi"),
              FileManager.default.fileExists(atPath: root.appendingPathComponent(filename).path) else { return }
        webView.loadFileURL(root.appendingPathComponent(filename), allowingReadAccessTo: root)
    }

    override func hrv_removeFromSuperview() {
        webView.stopLoading()
        removeFromSuperview()
    }
}
