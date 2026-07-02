package com.middleware.panorama;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link CameraAngleBuilder} — verifies the JSON structure
 * produced for the /angles endpoint. Pure Java, no OpenCV needed.
 */
class CameraAngleBuilderTest {

    private static final String[] FOUR_CAMERAS = {"front", "rear", "left", "right"};

    @Test
    void buildAnglesJson_containsTotalCameras() {
        String json = CameraAngleBuilder.buildAnglesJson(FOUR_CAMERAS);
        assertTrue(json.contains("\"total_cameras\": 4"));
    }

    @Test
    void buildAnglesJson_containsFovPerCamera() {
        String json = CameraAngleBuilder.buildAnglesJson(FOUR_CAMERAS);
        assertTrue(json.contains("\"field_of_view_per_camera\": 90.0"));
    }

    @Test
    void buildAnglesJson_containsAngleUnit() {
        String json = CameraAngleBuilder.buildAnglesJson(FOUR_CAMERAS);
        assertTrue(json.contains("\"angle_unit\": \"degrees\""));
    }

    @Test
    void buildAnglesJson_containsAllLabels() {
        String json = CameraAngleBuilder.buildAnglesJson(FOUR_CAMERAS);
        for (String name : FOUR_CAMERAS) {
            assertTrue(json.contains("\"label\": \"" + name + "\""), "label " + name);
        }
    }

    @Test
    void buildAnglesJson_frontCameraAt0Degrees() {
        String json = CameraAngleBuilder.buildAnglesJson(new String[]{"front", "right", "rear", "left"});
        // front center should be 0
        assertTrue(json.contains("\"label\": \"front\""));
        assertTrue(json.contains("\"center_angle\": 0"));
    }

    @Test
    void buildAnglesJson_rearCameraAt180Degrees() {
        String json = CameraAngleBuilder.buildAnglesJson(new String[]{"front", "right", "rear", "left"});
        assertTrue(json.contains("\"center_angle\": 180"));
    }

    @Test
    void buildAnglesJson_rightCameraAt90Degrees() {
        String json = CameraAngleBuilder.buildAnglesJson(new String[]{"front", "right", "rear", "left"});
        assertTrue(json.contains("\"center_angle\": 90"));
    }

    @Test
    void buildAnglesJson_leftCameraAt270Degrees() {
        String json = CameraAngleBuilder.buildAnglesJson(new String[]{"front", "right", "rear", "left"});
        assertTrue(json.contains("\"center_angle\": 270"));
    }

    @Test
    void buildAnglesJson_positionsAreCorrect() {
        String json = CameraAngleBuilder.buildAnglesJson(FOUR_CAMERAS);
        assertTrue(json.contains("\"position\": \"forward-facing\""));
        assertTrue(json.contains("\"position\": \"backward-facing\""));
        assertTrue(json.contains("\"position\": \"left-side\""));
        assertTrue(json.contains("\"position\": \"right-side\""));
    }

    @Test
    void buildAnglesJson_twoCameras() {
        String json = CameraAngleBuilder.buildAnglesJson(new String[]{"front", "rear"});
        assertTrue(json.contains("\"total_cameras\": 2"));
        assertTrue(json.contains("\"field_of_view_per_camera\": 180.0"));
        assertTrue(json.contains("\"center_angle\": 0"));
        assertTrue(json.contains("\"center_angle\": 180"));
    }

    @Test
    void buildAnglesJson_isValidJson() {
        String json = CameraAngleBuilder.buildAnglesJson(FOUR_CAMERAS);
        assertTrue(json.startsWith("{"));
        assertTrue(json.endsWith("}"));
        assertEquals(countOccurrences(json, '{'), countOccurrences(json, '}'));
        assertEquals(countOccurrences(json, '['), countOccurrences(json, ']'));
    }

    @Test
    void getCenterAngle_knownLabels() {
        assertEquals(0, CameraAngleBuilder.getCenterAngle("front", 0, 4));
        assertEquals(90, CameraAngleBuilder.getCenterAngle("right", 1, 4));
        assertEquals(180, CameraAngleBuilder.getCenterAngle("rear", 2, 4));
        assertEquals(270, CameraAngleBuilder.getCenterAngle("left", 3, 4));
    }

    @Test
    void getCenterAngle_unknownLabel() {
        // Unknown label at index 1 with 4 cameras → 360/4 * 1 = 90
        assertEquals(90, CameraAngleBuilder.getCenterAngle("cam1", 1, 4));
    }

    @Test
    void normalizeAngle_negative() {
        assertEquals(315.0, CameraAngleBuilder.normalizeAngle(-45.0), 0.001);
    }

    @Test
    void normalizeAngle_over360() {
        assertEquals(45.0, CameraAngleBuilder.normalizeAngle(405.0), 0.001);
    }

    private int countOccurrences(String s, char c) {
        int count = 0;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == c) count++;
        }
        return count;
    }
}
