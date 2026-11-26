import SwiftUI

/// A pixel icon on the Design System's 11 by 11 grid, one-pixel stroke, drawn
/// at 24pt (2pt per pixel) in the current foreground color.
public struct KODevIcon: View {
    let rows: [String]
    let label: String?

    /// - Parameter label: What VoiceOver says. Nil hides the icon from it, for
    ///   an icon next to text.
    public init(_ rows: [String], label: String? = nil) {
        self.rows = rows
        self.label = label
    }

    public var body: some View {
        KODevPixelArt(rows: rows)
            .frame(width: 24, height: 24)
            .accessibilityHidden(label == nil)
            .accessibilityLabel(label ?? "")
    }
}

/// Pixel-art rows (a `#` per pixel) drawn at 2pt per pixel in the current
/// foreground color, centered in the frame it gets.
struct KODevPixelArt: View {
    let rows: [String]

    var body: some View {
        let columns = rows.map(\.count).max() ?? 0
        Canvas { context, size in
            let origin = CGPoint(x: (size.width - CGFloat(columns * 2)) / 2, y: (size.height - CGFloat(rows.count * 2)) / 2)
            var path = Path()
            for (y, row) in rows.enumerated() {
                for (x, char) in row.enumerated() where char == "#" {
                    path.addRect(CGRect(x: origin.x + CGFloat(x * 2), y: origin.y + CGFloat(y * 2), width: 2, height: 2))
                }
            }
            context.fill(path, with: .foreground)
        }
    }
}

/// The Design System's interface icons. Draw new ones the same way: 11 strings
/// of 11 characters, a `#` for each pixel. Same data as kodev_ui's KODevIcons.
public enum KODevIcons {
    public static let arrowLeft = ["...........", "...........", "....#......", "...#.......", "..#........", ".#########.", "..#........", "...#.......", "....#......", "...........", "..........."]
    public static let chevronLeft = ["...........", "...........", "......#....", ".....#.....", "....#......", "...#.......", "....#......", ".....#.....", "......#....", "...........", "..........."]
    public static let chevronRight = ["...........", "...........", "....#......", ".....#.....", "......#....", ".......#...", "......#....", ".....#.....", "....#......", "...........", "..........."]
    public static let chevronDown = ["...........", "...........", "...........", "..#.....#..", "...#...#...", "....#.#....", ".....#.....", "...........", "...........", "...........", "..........."]
    public static let menu = ["...........", "...........", ".#########.", "...........", "...........", ".#########.", "...........", "...........", ".#########.", "...........", "..........."]
    public static let close = ["...........", ".#.......#.", "..#.....#..", "...#...#...", "....#.#....", ".....#.....", "....#.#....", "...#...#...", "..#.....#..", ".#.......#.", "..........."]
    public static let plus = ["...........", ".....#.....", ".....#.....", ".....#.....", ".....#.....", ".#########.", ".....#.....", ".....#.....", ".....#.....", ".....#.....", "..........."]
    public static let check = ["...........", "...........", "...........", ".........#.", "........#..", ".#.....#...", "..#...#....", "...#.#.....", "....#......", "...........", "..........."]
    public static let search = ["...........", "..####.....", ".#....#....", ".#....#....", ".#....#....", ".#....#....", "..####.#...", "........#..", ".........#.", "..........#", "..........."]
    public static let home = ["...........", ".....#.....", "....#.#....", "...#...#...", "..#.....#..", ".#.......#.", "..#.....#..", "..#.###.#..", "..#.#.#.#..", "..###.###..", "..........."]
    public static let user = ["...........", "....###....", "...#...#...", "...#...#...", "...#...#...", "....###....", "...........", "..#######..", ".#.......#.", ".#########.", "..........."]
    public static let bell = [".....#.....", "....###....", "...#...#...", "..#.....#..", "..#.....#..", "..#.....#..", "..#.....#..", ".#.......#.", ".#########.", "...........", "....###...."]
    public static let heart = ["...........", "...........", "..##...##..", ".#..#.#..#.", ".#...#...#.", ".#.......#.", "..#.....#..", "...#...#...", "....#.#....", ".....#.....", "..........."]
    public static let more = ["...........", "...........", "...........", "...........", "...........", ".##..##..##", ".##..##..##", "...........", "...........", "...........", "..........."]
}
