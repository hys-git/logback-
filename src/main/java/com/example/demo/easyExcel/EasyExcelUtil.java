package com.example.demo.easyExcel;

import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.metadata.*;
import com.alibaba.excel.support.ExcelTypeEnum;
import lombok.var;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author Gao
 */
public class EasyExcelUtil {

	/**
	 * 读取 Excel(多个 sheet)
	 * 将多sheet合并成一个list数据集，通过自定义ExcelReader继承AnalysisEventListener 重写invoke
	 * doAfterAllAnalysed方法 getExtendsBeanList 主要是做Bean的属性拷贝
	 * ，可以通过ExcelReader中添加的数据集直接获取
	 *
	 * @param excel    文件
	 * @param rowModel 实体类映射，继承 BaseRowModel 类
	 * @return Excel 数据 list
	 */
	public static <T extends BaseRowModel> List<T> readExcel(MultipartFile excel, Class<T> rowModel) throws Exception {
		ExcelListener excelListener = new ExcelListener();
		ExcelReader reader = getReader(excel, excelListener);
		if (reader == null) {
			return new ArrayList<>();
		}
		for (Sheet sheet : reader.getSheets()) {
			sheet.setClazz(rowModel);
			reader.read(sheet);
		}
		return getExtendsBeanList(excelListener.getDataList(), rowModel);
	}

	/**
	 * 读取某个 sheet 的 Excel
	 *
	 * @param excel    文件
	 * @param rowModel 实体类映射，继承 BaseRowModel 类
	 * @param sheetNo  sheet 的序号 从1开始
	 * @return Excel 数据 list
	 */
	public static <T extends BaseRowModel> List<T> readExcel(MultipartFile excel, Class<T> rowModel, int sheetNo) throws Exception {
		return readExcel(excel, rowModel, sheetNo, 1);
	}

	/**
	 * 利用BeanCopy转换list
	 */
	public static <T extends BaseRowModel> List<T> getExtendsBeanList(List<?> list, Class<T> typeClazz) {
		return BeanCopy.convert(list, typeClazz);
	}

	/**
	 * 读取某个 sheet 的 Excel
	 *
	 * @param excel       文件
	 * @param rowModel    实体类映射，继承 BaseRowModel 类
	 * @param sheetNo     sheet 的序号 从1开始
	 * @param headLineNum 表头行数，默认为1
	 * @return Excel 数据 list
	 */
	public static <T extends BaseRowModel> List<T> readExcel(MultipartFile excel, Class<T> rowModel, int sheetNo,
															 int headLineNum) throws Exception {
		ExcelListener excelListener = new ExcelListener();
		ExcelReader reader = getReader(excel, excelListener);
		if (reader == null) {
			return new ArrayList<>();
		}
		reader.read(new Sheet(sheetNo, headLineNum, rowModel));
		return getExtendsBeanList(excelListener.getDataList(), rowModel);
	}

	/**
	 * 导出 Excel ：一个 sheet，带表头 自定义WriterHandler 可以定制行列数据进行灵活化操作
	 * 
	 * @param response  HttpServletResponse
	 * @param list      数据 list，每个元素为一个 BaseRowModel
	 * @param fileName  导出的文件名
	 * @param sheetName 导入文件的 sheet 名
	 */
	public static <T extends BaseRowModel> void writeExcel(HttpServletResponse response, List<T> list, String fileName,
			String sheetName, ExcelTypeEnum excelTypeEnum, Class<T> classType) throws Exception {
		if (sheetName == null || "".equals(sheetName)) {
			sheetName = "sheet1";
		}

		ExcelWriterFactory writer = new ExcelWriterFactory(getOutputStream(fileName, response, excelTypeEnum),
				excelTypeEnum);
		Sheet sheet = new Sheet(1, 0, classType);
		sheet.setSheetName(sheetName);
		try {
			writer.write(list, sheet);
		} finally {
			writer.finish();
			writer.close();
		}

	}

	/**
	 * 導出Excel
	 * 
	 * @param <T>
	 * @param headList 頭部字段 key顯示名稱 value字段名稱
	 * @param list     要導出的列表
	 * @param fileName 文件名稱
	 * @throws NoSuchFieldException
	 * @throws SecurityException
	 */
	public static <T> void writeExcel(Map<String, String> headList, List<T> list, String fileName) throws Exception {
		List<List<String>> newHeadList = new ArrayList<List<String>>();
		List<List<Object>> rowsList = new ArrayList<List<Object>>();
		for (String columnName : headList.keySet()) {
			newHeadList.add(new ArrayList<String>() {
				private static final long serialVersionUID = 1L;
				{
					add(columnName);
				}
			});
		}
		if (list != null && list.size() > 0) {
			rowsList = list.stream().map(a -> {
				List<Object> row = new ArrayList<Object>();
				for (String fieldName : headList.values()) {
					if (fieldName.contains("{") && fieldName.contains("}")) {
						String pattern = "\\{(.*?)\\}";
						Pattern r = Pattern.compile(pattern);
						Matcher matcher = r.matcher(fieldName);
						StringBuffer sb = new StringBuffer();
						while (matcher.find()) {
							String v = matcher.group();
							Object tnObject = getValue(a, v.replace("{", "").replace("}", ""));
							matcher.appendReplacement(sb, tnObject == null ? "" : tnObject.toString());
						}
						matcher.appendTail(sb);
						row.add(sb.toString());

					} else {
						row.add(getFieldValue(a, fieldName));
					}
				}
				return row;
			}).collect(Collectors.toList());
		}
		writeExcel(newHeadList, rowsList, fileName);
	}

	/**
	 * 反射获取字段值（兼容Map<K, V>类型的字段）
	 * 
	 * @param model
	 * @param fieldName
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> Object getFieldValue(T model, String fieldName) {
		if (fieldName == null || "".equals(fieldName)) {
			return null;
		}
		// 用于分隔Map类型的字段名与Key的分隔符（例如：productEstimateMap$投资决策系统）
		String separator = "$";
		Field field = null;

		try {
			Class<?> clazz = model.getClass();

			if (fieldName.contains(separator)) {
				var fieldNameList = fieldName.split(("\\" + separator));
				for (; clazz != Object.class; clazz = clazz.getSuperclass()) {
					try {
						field = clazz.getDeclaredField(fieldNameList[0]);
					} catch (Exception e) {
					}
				}
				if (field == null || !Map.class.isAssignableFrom(field.getType())) {
					return null;
				}
				field.setAccessible(true);
				var mapField = (Map<String, BigDecimal>) field.get(model);
				if (mapField == null) {
					return null;
				}
				return mapField.get(fieldNameList[1]);
			} else {
				for (; clazz != Object.class; clazz = clazz.getSuperclass()) {
					try {
						field = clazz.getDeclaredField(fieldName);
					} catch (Exception e) {
					}
				}
				if (field == null) {
					return null;
				}
				field.setAccessible(true);
				var val = field.get(model);
				if (val != null && field.getType() == Date.class) {
					SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
					return simpleDateFormat.format(val);
				}
				return val;
			}
		} catch (NullPointerException e) {
			return null;
		} catch (IllegalAccessException e) {
			return null;
		}
	}

	/**
	 * 获取某个字段的值
	 * 
	 * @param <T>
	 * @param model
	 * @param field
	 * @return
	 */
	public static <T> Object getValue(T model, String field) {
		if (field == null || "".equals(field)) {
			return null;
		}
		// 拼接方法
		field = new StringBuffer("get").append(field.substring(0, 1).toUpperCase()).append(field.substring(1))
				.toString();
		try {
			Method method = getMethod(field, model.getClass());
			Object s = method.invoke(model);
			if (s == null) {
				return null;
			}
			if (s instanceof Date) {
				SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
				return simpleDateFormat.format(s);
			}
			return s;

		} catch (NoSuchMethodException e) {
			return null;
		} catch (NullPointerException e) {
			return null;
		} catch (InvocationTargetException e) {
			return null;
		} catch (IllegalAccessException e) {
			return null;
		}
	}

	private static <T> Method getMethod(String methodName, Class<T> clasType) throws NoSuchMethodException {
		for (Class<?> superClass = clasType; superClass != Object.class; superClass = superClass.getSuperclass()) {
			try {
				return superClass.getDeclaredMethod(methodName);
			} catch (NoSuchMethodException e) {
				// Method不在当前类定义,继续向上转型
			}
		}
		throw new NoSuchMethodException();
	}

	public static void writeExcel(List<List<String>> headList, List<List<Object>> list, String fileName) throws Exception {
		HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
				.getResponse();
		writeExcel(response, headList, list, fileName, fileName, ExcelTypeEnum.XLSX);
	}

	public static void writeExcel(HttpServletResponse response, List<List<String>> headList, List<List<Object>> list, String fileName) throws Exception {
		writeExcel(response, headList, list, fileName, fileName, ExcelTypeEnum.XLSX);
	}

	public static void writeExcel(HttpServletResponse response, List<List<String>> headList, List<List<Object>> list, String fileName, String sheetName) throws Exception {
		writeExcel(response, headList, list, fileName, sheetName, ExcelTypeEnum.XLSX);
	}

	/**
	 * 
	 * @param response
	 * @param headList      表头
	 * @param list          数据
	 * @param fileName
	 * @param sheetName
	 * @param excelTypeEnum
	 */
	public static void writeExcel(HttpServletResponse response, List<List<String>> headList, List<List<Object>> list, String fileName, String sheetName,
			ExcelTypeEnum excelTypeEnum) throws Exception {
		if (sheetName == null || "".equals(sheetName)) {
			sheetName = "sheet1";
		}
		//ExcelWriter writer = new ExcelWriter(null, null, ExcelTypeEnum.XLSX, true, null);
		ExcelWriterFactory writer = new ExcelWriterFactory(getOutputStream(fileName, response, excelTypeEnum),
				excelTypeEnum,null);
		Table table1 = new Table(1);
		table1.setHead(headList);
		Sheet sheet = new Sheet(1, 0);
		sheet.setSheetName(sheetName);
		try {
			writer.write1(list, sheet, table1);
		} finally {
			writer.finish();
			writer.close();
		}

	}

	/**
	 * 导出 Excel ：多个 sheet，带表头
	 * 
	 * @param response  HttpServletResponse
	 * @param list      数据 list，每个元素为一个 BaseRowModel
	 * @param fileName  导出的文件名
	 * @param sheetName 导入文件的 sheet 名
	 * @param object    映射实体类，Excel 模型
	 */
	public static ExcelWriterFactory writeExcelWithSheets(HttpServletResponse response,
			List<? extends BaseRowModel> list, String fileName, String sheetName, BaseRowModel object,
			ExcelTypeEnum excelTypeEnum) throws Exception {
		ExcelWriterFactory writer = new ExcelWriterFactory(getOutputStream(fileName, response, excelTypeEnum),
				excelTypeEnum);
		Sheet sheet = new Sheet(1, 0, object.getClass());
		sheet.setSheetName(sheetName);
		writer.write(list, sheet);
		return writer;
	}

	/**
	 * 导出文件时为Writer生成OutputStream
	 */
	private static OutputStream getOutputStream(String fileName, HttpServletResponse response,
												ExcelTypeEnum excelTypeEnum) throws Exception {
		// 创建本地文件
		String filePath = fileName + excelTypeEnum.getValue();
		try {
			fileName = new String(filePath.getBytes(), "ISO-8859-1");
			response.addHeader("Content-Disposition", "filename=" + fileName);
			response.setContentType("application/octet-stream");
			response.setContentType("application/x-download");
			response.setCharacterEncoding("UTF-8");
			return response.getOutputStream();
		} catch (IOException e) {
			throw new Exception("ERROR");
		}
	}

	/**
	 * 返回 ExcelReader
	 * 
	 * @param excel         需要解析的 Excel 文件
	 * @param excelListener new ExcelListener()
	 */
	private static ExcelReader getReader(MultipartFile excel, ExcelListener excelListener) throws Exception {
		String fileName = excel.getOriginalFilename();
		if (fileName == null) {
			throw new Exception("文件格式错误！");
		}
		if (!fileName.toLowerCase().endsWith(ExcelTypeEnum.XLS.getValue())
				&& !fileName.toLowerCase().endsWith(ExcelTypeEnum.XLSX.getValue())) {
			throw new Exception("文件格式错误！");
		}
		InputStream inputStream;
		try {
			inputStream = excel.getInputStream();
			return new ExcelReader(inputStream, null, excelListener, false);
		} catch (IOException e) {
			// do something
		}
		return null;
	}


	public static TableStyle createTableStyle() {
		TableStyle tableStyle = new TableStyle();
		// 设置表头样式
		Font headFont = new Font();
		// 字体是否加粗
		headFont.setBold(true);
		// 字体大小
		headFont.setFontHeightInPoints((short) 11);
		tableStyle.setTableHeadFont(headFont);
		// 背景色
		tableStyle.setTableHeadBackGroundColor(IndexedColors.WHITE);

		// 设置表格主体样式
		Font contentFont = new Font();
		contentFont.setBold(true);
		contentFont.setFontHeightInPoints((short) 11);
		tableStyle.setTableContentFont(contentFont);
		tableStyle.setTableContentBackGroundColor(IndexedColors.WHITE);
		return tableStyle;
	}

}
