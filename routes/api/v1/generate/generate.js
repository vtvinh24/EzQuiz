const express = require("express");
const { GoogleGenerativeAI } = require("@google/generative-ai");
require("dotenv").config(); // Để tải biến môi trường (GEMINI_API_KEY)
const multer = require("multer"); // Import multer

const QuizRouter = express.Router();

// 1. Khởi tạo Gemini AI
// Lấy API key từ biến môi trường
const genAI = new GoogleGenerativeAI(process.env.GEMINI_API_KEY);

// Khởi tạo model AI
// Sử dụng gemini-pro-vision nếu muốn khả năng hiểu hình ảnh tốt hơn và tạo câu hỏi phức tạp hơn từ hình ảnh.
// Hoặc gemini-2.5-flash nếu ưu tiên tốc độ, nó vẫn hỗ trợ đa phương tiện.
const model = genAI.getGenerativeModel({ model: "gemini-2.5-flash" });
// const model = genAI.getGenerativeModel({ model: "gemini-pro-vision" }); // Lựa chọn thay thế nếu cần khả năng thị giác mạnh mẽ hơn

// 2. System Prompt để "hướng dẫn" AI trả về đúng định dạng JSON
const QUIZ_GENERATOR_SYSTEM_PROMPT = `
Bạn là một chuyên gia tạo câu hỏi trắc nghiệm. Nhiệm vụ của bạn là tạo ra một danh sách các câu hỏi dựa trên thông tin người dùng cung cấp, có thể bao gồm văn bản hoặc hình ảnh.
BẠN PHẢI trả lời bằng một đối tượng JSON hợp lệ. KHÔNG được thêm bất kỳ văn bản giới thiệu, lời kết, hay các khối mã markdown như \`\`\`json.
Đối tượng JSON phải có một khóa duy nhất là "quizzes".
Giá trị của "quizzes" phải là một MẢNG (array) các đối tượng câu hỏi.

Mỗi đối tượng câu hỏi trong mảng phải có cấu trúc như sau:
- "question": (string) Nội dung câu hỏi.
- "answers": (array of strings) Một danh sách các câu trả lời khả thi.
- "correctAnswerIndices": (array of numbers) Một mảng chứa chỉ số (bắt đầu từ 0) của (các) câu trả lời đúng trong mảng "answers". Với câu hỏi một lựa chọn, mảng này chỉ có một số. Với câu hỏi nhiều lựa chọn, nó có thể có nhiều số.
- "type": (string) Phải là "SINGLE_CHOICE" hoặc "MULTIPLE_CHOICE".

Đây là một ví dụ về định dạng đầu ra BẮT BUỘC cho chủ đề "Hệ Mặt Trời":
{
  "quizzes": [
    {
      "question": "Hành tinh nào được mệnh danh là 'Hành tinh Đỏ'?",
      "answers": ["Trái Đất", "Sao Hỏa", "Sao Mộc", "Sao Kim"],
      "correctAnswerIndices": [1],
      "type": "SINGLE_CHOICE"
    },
    {
      "question": "Những hành tinh nào sau đây là hành tinh khí khổng lồ?",
      "answers": ["Sao Hỏa", "Sao Mộc", "Sao Thổ", "Sao Hải Vương"],
      "correctAnswerIndices": [1, 2, 3],
      "type": "MULTIPLE_CHOICE"
    }
  ]
}
`;

// Cấu hình Multer để lưu trữ file trong bộ nhớ (memoryStorage).
// Điều này phù hợp cho việc xử lý các file nhỏ/vừa mà không cần ghi ra đĩa.
const storage = multer.memoryStorage();
const upload = multer({ storage: storage });

// 3. API Route để tạo Quiz
// Sử dụng middleware `upload.single("file")` để xử lý một file duy nhất với tên trường là "file"
QuizRouter.post("/generate-quiz", upload.single("file"), async (req, res) => {
  try {
    // 1. Lấy prompt (text) từ body và file từ req.file
    // `prompt` là văn bản người dùng cung cấp (ví dụ: "Tạo câu hỏi về bức ảnh này")
    const { prompt } = req.body;
    // `file` là đối tượng file được upload qua multer
    const file = req.file;

    // Kiểm tra xem có ít nhất một prompt text hoặc một file được cung cấp hay không
    if (!prompt && !file) {
      return res.status(400).json({
        message: "Vui lòng cung cấp chủ đề (văn bản) hoặc một file (hình ảnh).",
      });
    }

    // Tạo mảng 'contents' để gửi cho Gemini API.
    // Gemini API hỗ trợ gửi cả văn bản và dữ liệu hình ảnh trong cùng một yêu cầu.
    const contents = [];

    // Thêm prompt text nếu có
    if (prompt) {
      contents.push({ text: prompt });
    }

    // Thêm file (hình ảnh) nếu có
    if (file) {
      // Kiểm tra loại file (chỉ chấp nhận hình ảnh)
      if (!file.mimetype.startsWith("image/")) {
        return res.status(400).json({
          message: "Chỉ chấp nhận file hình ảnh (JPEG, PNG, WEBP, HEIC).",
        });
      }

      // Chuyển đổi buffer của file thành chuỗi Base64
      const base64Data = file.buffer.toString("base64");

      // Thêm dữ liệu hình ảnh vào mảng contents dưới dạng inlineData
      contents.push({
        inlineData: {
          mimeType: file.mimetype, // Ví dụ: "image/jpeg", "image/png"
          data: base64Data, // Chuỗi Base64 của hình ảnh
        },
      });
    }

    // 2. Bắt đầu phiên chat với AI, đặt vai trò hệ thống cho nó
    // Lịch sử chat được giữ nguyên để AI hiểu định dạng đầu ra mong muốn.
    const chat = model.startChat({
      history: [
        { role: "user", parts: [{ text: QUIZ_GENERATOR_SYSTEM_PROMPT }] },
        {
          role: "model",
          parts: [
            {
              text: "OK. I understand. I will generate quizzes in the specified JSON format.",
            },
          ],
        },
      ],
    });

    // 3. Gửi prompt của người dùng (có thể bao gồm text và hình ảnh)
    // chat.sendMessage giờ đây nhận một mảng `contents` (text và/hoặc inlineData)
    const result = await chat.sendMessage(contents);
    const aiResponseText = result.response.text();

    // 4. Parse chuỗi JSON từ AI trả về
    let quizData;
    try {
      // AI đôi khi vẫn trả về kèm markdown, nên chúng ta cần làm sạch nó trước khi parse.
      const cleanedJsonString = aiResponseText
        .replace(/```json/g, "")
        .replace(/```/g, "")
        .trim();
      quizData = JSON.parse(cleanedJsonString);
    } catch (parseError) {
      console.error("Lỗi parse JSON từ AI:", parseError);
      console.error("Dữ liệu gốc từ AI:", aiResponseText); // In ra để debug
      return res.status(500).json({
        message: "Trợ lý AI đã trả về dữ liệu không hợp lệ. Vui lòng thử lại với chủ đề khác hoặc một hình ảnh khác.",
      });
    }

    // 5. Trả về đối tượng JSON cho client
    res.status(200).json(quizData);
    console.log("Quiz generated successfully:", JSON.stringify(quizData, null, 2));
  } catch (error) {
    console.error("Lỗi từ Gemini API hoặc logic generate-quiz:", error);
    // Kiểm tra các lỗi phổ biến từ Gemini API
    if (error.response && error.response.status) {
      console.error("Gemini API Status:", error.response.status);
      console.error("Gemini API Data:", error.response.data);
      return res.status(error.response.status).json({ message: error.response.data || "Lỗi từ Gemini API." });
    }
    res.status(500).json({ message: "Có lỗi xảy ra khi kết nối với trợ lý AI." });
  }
});

// 4. Export router để có thể sử dụng ở file khác (ví dụ: app.js)
module.exports = QuizRouter;
