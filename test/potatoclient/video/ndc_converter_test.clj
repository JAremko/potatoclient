(ns potatoclient.video.ndc-converter-test
  (:require [clojure.test :refer [deftest testing is]])
  (:import [potatoclient.video NDCConverter NDCConverter$NDCPoint NDCConverter$PixelPoint]))

(deftest pixel-to-ndc-test
  (testing "Center pixel converts to origin"
    (let [ndc (NDCConverter/pixelToNDC 400 300 800 600)]
      (is (= 0.0 (.x ndc)))
      (is (= 0.0 (.y ndc)))))
  
  (testing "Top-left corner"
    (let [ndc (NDCConverter/pixelToNDC 0 0 800 600)]
      (is (= -1.0 (.x ndc)))
      (is (= 1.0 (.y ndc)))))
  
  (testing "Bottom-right corner"
    (let [ndc (NDCConverter/pixelToNDC 800 600 800 600)]
      (is (= 1.0 (.x ndc)))
      (is (= -1.0 (.y ndc)))))
  
  (testing "Quarter points"
    ;; Top-center
    (let [ndc (NDCConverter/pixelToNDC 400 0 800 600)]
      (is (= 0.0 (.x ndc)))
      (is (= 1.0 (.y ndc))))
    
    ;; Right-center
    (let [ndc (NDCConverter/pixelToNDC 800 300 800 600)]
      (is (= 1.0 (.x ndc)))
      (is (= 0.0 (.y ndc))))))

(deftest ndc-to-pixel-test
  (testing "Origin converts to center"
    (let [pixel (NDCConverter/ndcToPixel 0.0 0.0 800 600)]
      (is (= 400 (.x pixel)))
      (is (= 300 (.y pixel)))))
  
  (testing "Corners"
    ;; Top-left
    (let [pixel (NDCConverter/ndcToPixel -1.0 1.0 800 600)]
      (is (= 0 (.x pixel)))
      (is (= 0 (.y pixel))))
    
    ;; Bottom-right
    (let [pixel (NDCConverter/ndcToPixel 1.0 -1.0 800 600)]
      (is (= 800 (.x pixel)))
      (is (= 600 (.y pixel))))))

(deftest pixel-delta-to-ndc-test
  (testing "Horizontal movement"
    ;; 80 pixels in 800 width = 0.2 NDC units
    (let [ndc (NDCConverter/pixelDeltaToNDC 80 0 800 600)]
      (is (= 0.2 (.x ndc)))
      (is (= 0.0 (.y ndc)))))
  
  (testing "Vertical movement"
    ;; 60 pixels in 600 height = 0.2 NDC units (inverted)
    (let [ndc (NDCConverter/pixelDeltaToNDC 0 60 800 600)]
      (is (= 0.0 (.x ndc)))
      (is (= -0.2 (.y ndc)))))
  
  (testing "Diagonal movement"
    (let [ndc (NDCConverter/pixelDeltaToNDC 40 30 800 600)]
      (is (= 0.1 (.x ndc)))
      (is (= -0.1 (.y ndc))))))

(deftest round-trip-conversion-test
  (testing "Pixel to NDC and back"
    (let [original-x 123
          original-y 456
          ndc (NDCConverter/pixelToNDC original-x original-y 800 600)
          pixel (NDCConverter/ndcToPixel (.x ndc) (.y ndc) 800 600)]
      (is (= original-x (.x pixel)))
      (is (= original-y (.y pixel)))))
  
  (testing "NDC to pixel and back"
    (let [original-x 0.5
          original-y -0.5
          pixel (NDCConverter/ndcToPixel original-x original-y 800 600)
          ndc (NDCConverter/pixelToNDC (.x pixel) (.y pixel) 800 600)]
      (is (< (Math/abs (- original-x (.x ndc))) 0.001))
      (is (< (Math/abs (- original-y (.y ndc))) 0.001)))))

(deftest edge-cases-test
  (testing "Zero-sized canvas"
    ;; Should handle gracefully, typically returning 0,0
    (let [ndc (NDCConverter/pixelToNDC 0 0 0 0)]
      (is (Double/isNaN (.x ndc)))
      (is (Double/isNaN (.y ndc)))))
  
  (testing "Very large canvas"
    (let [ndc (NDCConverter/pixelToNDC 1920 1080 3840 2160)]
      (is (= 0.0 (.x ndc)))
      (is (= 0.0 (.y ndc))))))