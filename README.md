# CMPT 365 Term Project: Video Compression

## Objective:
Learn essential parts of MPEG and H.264/H.265 video compression, and some multimedia programming skills.
## Assignment:
* Implement video compression with motion compensation, transform coding, and quantization for your H.26* encoder and decoder.
* Use 4:2:0 for chroma subsampling.
* Choose a video frame sequence (I-, P-, B-frames) similar to MPEG-1, 2.   No interlacing.
  * For I-frames, use a simplified H. 264 Intra_4 × 4 predictive coding, e.g., with three modes.
  * For P- and B- frames, use only 8 × 8 for motion estimation. Use logarithmic search for motion vectors. Afterwards, use the 4 × 4 integer transform in H.264 (not DCT).
* Combine scaling and quantization as suggested in the Handout.
* Control and show the effect of various levels of compression and quantization losses.
* Do not implement the entropy coding part. Optionally, you may include any publicly available code for this.
* Create a graphical user interface for the purpose of demonstrating your results.
* Choose some short video clips (e.g., 5 to 30 seconds) as your test data. Start with something simple, e.g. CIF/QCIF, and smaller FPS (frames per second), followed by some good test videos (higher resolution, higher quality).
