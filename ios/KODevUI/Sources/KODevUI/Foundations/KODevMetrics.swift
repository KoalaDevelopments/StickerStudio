import CoreGraphics

/// The spacing scale, base 4.
public enum KODevSpace {
    /// Between an icon and its text.
    public static let space1: CGFloat = 4
    /// Inside small controls and tags.
    public static let space2: CGFloat = 8
    /// Vertical padding of buttons and fields.
    public static let space3: CGFloat = 12
    /// Card padding; screen margin on a phone.
    public static let space4: CGFloat = 16
    /// Side padding of buttons; gap between groups of content.
    public static let space6: CGFloat = 24
    /// Gap between cards side by side.
    public static let space8: CGFloat = 32
    /// Gap between blocks of a section.
    public static let space12: CGFloat = 48
    /// Between sections on phones and tablets.
    public static let space16: CGFloat = 64
    /// Between sections on the desktop.
    public static let space30: CGFloat = 120
}

/// Corner radii. Square is the default: the brand is made of pixels.
public enum KODevRadius {
    /// Buttons, fields, tags.
    public static let none: CGFloat = 0
    /// Cards and panels.
    public static let md: CGFloat = 16
    /// Showcases and large containers.
    public static let lg: CGFloat = 48
    /// Avatars.
    public static let full: CGFloat = 9999
}
