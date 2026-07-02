package com.middleware.panorama;

/**
 * Builds a JSON response containing camera feed labels (front, rear, right, left)
 * and their respective viewing angles within the 360-degree panorama.
 *
 * Angle convention:
 * - Angles are measured clockwise from the forward direction (0 degrees).
 * - Each camera covers a field-of-view (FOV) segment of the full 360 degrees.
 * - For a standard 4-camera setup: each camera covers 90 degrees.
 *
 * Default mapping:
 *   front → center 0°,   range [315°, 45°]
 *   right → center 90°,  range [45°, 135°]
 *   rear  → center 180°, range [135°, 225°]
 *   left  → center 270°, range [225°, 315°]
 */
public final class CameraAngleBuilder {

    private CameraAngleBuilder() {}

    /**
     * Standard 4-camera directional labels with their center angles.
     */
    private static final String[] DEFAULT_LABELS = {"front", "right", "rear", "left"};
    private static final int[] DEFAULT_CENTER_ANGLES = {0, 90, 180, 270};

    /**
     * Build the camera-angles JSON for the given camera names.
     * Camera names are matched to directional positions (front, rear, right, left)
     * and each is assigned the corresponding angle metadata.
     *
     * @param cameraNames array of camera names in stitch order (e.g. {"front","rear","left","right"})
     * @return pretty-printed JSON string with labels and angles
     */
    public static String buildAnglesJson(String[] cameraNames) {
        int numCameras = cameraNames.length;
        double fov = 360.0 / numCameras;

        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"total_cameras\": ").append(numCameras).append(",\n");
        sb.append("  \"field_of_view_per_camera\": ").append(fov).append(",\n");
        sb.append("  \"angle_unit\": \"degrees\",\n");
        sb.append("  \"angle_convention\": \"clockwise_from_front\",\n");
        sb.append("  \"cameras\": [\n");

        for (int i = 0; i < numCameras; i++) {
            String label = cameraNames[i].toLowerCase();
            int centerAngle = getCenterAngle(label, i, numCameras);
            double startAngle = normalizeAngle(centerAngle - fov / 2.0);
            double endAngle = normalizeAngle(centerAngle + fov / 2.0);

            sb.append("    {\n");
            sb.append("      \"label\": \"").append(label).append("\",\n");
            sb.append("      \"position\": \"").append(getPosition(label)).append("\",\n");
            sb.append("      \"center_angle\": ").append(centerAngle).append(",\n");
            sb.append("      \"start_angle\": ").append(formatAngle(startAngle)).append(",\n");
            sb.append("      \"end_angle\": ").append(formatAngle(endAngle)).append(",\n");
            sb.append("      \"field_of_view\": ").append(fov).append("\n");
            sb.append("    }");
            if (i < numCameras - 1) sb.append(",");
            sb.append("\n");
        }

        sb.append("  ]\n}");
        return sb.toString();
    }

    /**
     * Get the center angle for a camera based on its directional label.
     * Known labels (front, rear, right, left) get their standard angles.
     * Unknown labels are assigned angles evenly distributed around 360 degrees.
     */
    static int getCenterAngle(String label, int index, int numCameras) {
        switch (label) {
            case "front": return 0;
            case "right": return 90;
            case "rear":  return 180;
            case "left":  return 270;
            default:
                return (int) ((360.0 / numCameras) * index);
        }
    }

    /**
     * Map a label to a human-readable position description.
     */
    static String getPosition(String label) {
        switch (label) {
            case "front": return "forward-facing";
            case "right": return "right-side";
            case "rear":  return "backward-facing";
            case "left":  return "left-side";
            default:      return "custom";
        }
    }

    /**
     * Normalize an angle to the [0, 360) range.
     */
    static double normalizeAngle(double angle) {
        double result = angle % 360.0;
        if (result < 0) result += 360.0;
        return result;
    }

    /**
     * Format an angle value: if it's a whole number, output as integer;
     * otherwise output with one decimal place.
     */
    private static String formatAngle(double angle) {
        if (angle == Math.floor(angle)) {
            return String.valueOf((int) angle);
        }
        return String.format("%.1f", angle);
    }
}
