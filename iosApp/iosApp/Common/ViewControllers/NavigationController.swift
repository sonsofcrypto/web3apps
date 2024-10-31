// Created by web3d3v on 13/02/2022.
// Copyright (c) 2022 Sons Of Crypto.
// SPDX-License-Identifier: MIT

import UIKit
import web3walletcore

final class NavigationItem: UINavigationItem {
    @IBOutlet var contentView: UIView?
}

final class NavigationController: UINavigationController {

    weak var contentView: UIView? {
        didSet { didSetContentView(contentView, prevView: oldValue) }
    }

    override var preferredStatusBarStyle: UIStatusBarStyle {
        Theme.statusBarStyle
    }

    private weak var toastView: ToastView? = nil

    override func viewDidLoad() {
        super.viewDidLoad()
        let appearance = navigationBar.standardAppearance
        appearance.backgroundColor = Theme.color.navBarBackground
        appearance.titleTextAttributes = [
            .foregroundColor: Theme.color.navBarTitle,
            .font: Theme.font.navTitle
        ]
        appearance.setBackIndicatorImage(
            UIImage(systemName: "chevron.left"),
            transitionMaskImage: UIImage(systemName: "chevron.left")
        )

        navigationBar.standardAppearance = appearance
        navigationBar.scrollEdgeAppearance = appearance
        navigationBar.compactAppearance = appearance
        navigationBar.compactScrollEdgeAppearance = appearance
        navigationBar.tintColor = Theme.color.navBarTint
        interactivePopGestureRecognizer?.delegate = self
    }

    override func viewDidLayoutSubviews() {
        super.viewDidLayoutSubviews()
        layoutBar()
    }

    override func pushViewController(_ viewController: UIViewController, animated: Bool) {
        super.pushViewController(viewController, animated: animated)
        contentView = contentView(for: viewController)
    }

    override func popViewController(animated: Bool) -> UIViewController? {
        let vc = super.popViewController(animated: animated)
        contentView = contentView(for: viewControllers.last)
        return vc
    }

    override func popToViewController(_ viewController: UIViewController, animated: Bool) -> [UIViewController]? {
        if let idx = viewControllers.firstIndex(of: viewController), idx > 0 {
            contentView = contentView(for: viewControllers[idx - 1])
        }
        let vcs = super.popToViewController(viewController, animated: animated)
        return vcs
    }

    override func popToRootViewController(animated: Bool) -> [UIViewController]? {
        contentView = contentView(for: viewControllers[safe: 0])
        let vcs = super.popToRootViewController(animated: animated)
        return vcs
    }
}

extension UINavigationController {

    var topVc: UIViewController? {
        topViewController
    }
}

// MARK: - UIGestureRecognizerDelegate

extension NavigationController: UIGestureRecognizerDelegate {

    func gestureRecognizerShouldBegin(
        _ gestureRecognizer: UIGestureRecognizer
    ) -> Bool {
        viewControllers.count > 1
    }
}

private extension NavigationController {

    func didSetContentView(_ contentView: UIView?, prevView: UIView?) {
        guard contentView != prevView else { return }

        if let contentView = contentView {
            let barFrame = navigationBar.frame
            let width = contentView.intrinsicContentSize.width
            view.addSubview(contentView)
            contentView.frame = CGRect(
                center: .init(x: barFrame.width.half, y: barFrame.midY),
                size: .init(width: width, height: 0)
            )
        }
        
        transitionCoordinator?.animate(
            alongsideTransition: { context in
                prevView?.alpha = 0
                contentView?.alpha = 1
                self.layoutBar()
                let ty = -(prevView?.bounds.height ?? 0.0)
                prevView?.transform = CGAffineTransformMakeTranslation(0, ty)
            },
            completion: { context in
                prevView?.transform = .identity
                prevView?.removeFromSuperview()
                self.layoutBar()
            }
        )
    }

    func layoutBar() {
        guard let contentView = contentView else {
            topVc?.additionalSafeAreaInsets = UIEdgeInsets.zero
            return
        }

        var barFrame = navigationBar.frame
        barFrame.size.height += contentView.intrinsicContentSize.height
        barFrame.size.height += Theme.padding
        navigationBar.frame = barFrame

        var contentFrame = barFrame.insetBy(dx: Theme.padding, dy: 0)
        contentFrame.size.height = contentView.intrinsicContentSize.height
        contentFrame.origin.y = barFrame.maxY - Theme.padding
        contentFrame.origin.y -= contentView.bounds.height
        contentView.frame = contentFrame

        topVc?.additionalSafeAreaInsets = UIEdgeInsets.with(
            top: barFrame.maxY - contentFrame.minY
        )
    }


    func contentView(for viewController: UIViewController?) -> UIView? {
        (viewController?.navigationItem as? NavigationItem)?.contentView
    }
}

// MARK: - Toast

extension NavigationController {

    func toast(_ text: String, media: ImageMedia?, top: Bool = true) {
        let toastView = ToastView(frame: view.bounds)
        view.insertSubview(toastView, belowSubview: navigationBar)
        toastView.update(text, media: media, top: top)
        toastView.bounds.size = toastView.intrinsicContentSize
        toastView.frame.origin.y = top
            ? navigationBar.frame.maxY
            : view.bounds.maxY - toastView.bounds.size.height
        
        let y = toastView.intrinsicContentSize.height
        toastView.transform = CGAffineTransformMakeTranslation(0, top ? -y : y)
        UIView.springAnimate { toastView.transform = .identity }

        self.toastView = toastView
        DispatchQueue.main.asyncAfter(deadline: .now() + 3.5) { [weak self] in
            toastView.removeAnimated()
            self?.toastView = nil
        }
    }

    func isPresentingToast() -> Bool {
        toastView != nil
    }

    func toast(_ viewModel: ToastViewModel) {
        toast(
            viewModel.text,
            media: viewModel.media,
            top: viewModel.position == .top
        )
    }

    private final class ToastView: UIScrollView {
        private lazy var bgView = ThemeBlurView().round()
        private lazy var imgView = UIImageView()
        private lazy var label = UILabel(
            Theme.font.body,
            color: Theme.color.textPrimary
        )
        private lazy var stack = HStackView(
            [imgView, label],
            spacing: Theme.padding
        )
        private var _contentSize = CGSize.zero
        private var top = true
        private var removing = false

        override init(frame: CGRect) {
            super.init(frame: frame)
            configureUI()
        }

        required init?(coder: NSCoder) {
            super.init(coder: coder)
            configureUI()
        }

        func update(_ text: String, media: ImageMedia?, top: Bool = true) {
            self.label.text = text
            self.top = top
            
            if let media = media {
                imgView.setImageMedia(media)
                imgView.isHidden = false
            }
            
            contentSize = updateContentSize()
            
            if top {
                contentInset.bottom = _contentSize.height
            } else {
                contentInset.top = _contentSize.height
            }
            
            contentOffset.y = 0
        }

        func removeAnimated() {
            guard !removing else { return }
            removing = true
            UIView.springAnimate(
                animations: { [weak self] in
                    let top = self?.top ?? true
                    let y = (self?._contentSize.height ?? 0) * (top ? -1 : 1)
                    self?.transform = CGAffineTransformMakeTranslation(0, y)
                    self?.alpha = 0
                },
                completion: { [weak self] _ in self?.removeFromSuperview() }
            )
        }
        
        override func layoutSubviews() {
            super.layoutSubviews()
            let padding = Theme.padding

            bgView.frame = .init(zeroOrigin: _contentSize)
                .insetBy(dx: padding.half, dy: padding)

            if !top {
                bgView.frame.size.height -= padding
            }

            stack.frame = bgView.frame.insetBy(dx: padding, dy: padding)
            label.preferredMaxLayoutWidth = stack.bounds.width
                - (imgView.isHidden ? 0 : Constant.imgWidth + stack.spacing)
            
            if top && contentOffset.y > _contentSize.height.half {
                removeAnimated()
            } else if !top && contentOffset.y < -_contentSize.height.half {
                removeAnimated()
            }
        }

        override var intrinsicContentSize: CGSize {
            return _contentSize
        }

        private func updateContentSize() -> CGSize {
            let padding = Theme.padding
            let width = bounds.width - padding * 3
            let imgWidth = imgView.isHidden ? 0 : Constant.imgWidth + padding
            let textSize = String.estimateSize(
                label.text,
                font: label.font,
                maxWidth: width - imgWidth,
                extraHeight: 0,
                minHeight: padding
            )
            _contentSize = .init(
                width: bounds.width,
                height: textSize.height + padding * 4 + (top ? 0 : padding)
            )
            return _contentSize
        }

        private func configureUI() {
            imgView.isHidden = true
            imgView.contentMode = .scaleAspectFit
            imgView.tintColor = label.textColor
            label.numberOfLines = 0
            label.lineBreakMode = .byWordWrapping
            
            [imgView, label, stack].forEach {
                $0.translatesAutoresizingMaskIntoConstraints = false
            }
            
            addSubview(bgView)
            addSubview(stack)

            isPagingEnabled = true
            showsVerticalScrollIndicator = false

            let w = Constant.imgWidth
            addConstraint(imgView.widthAnchor.constraint(equalToConstant: w))
        }
    }
    
    enum Constant {
        static let imgWidth: CGFloat = 34
    }
}
