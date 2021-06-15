package com.company.project.controller;

import com.company.project.common.utils.JSONUtil;
import com.company.project.common.utils.PDFHelper;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * PDFController
 * </p>
 *
 * @author wyf
 * @since 2021-06-06
 */
@RestController
@RequestMapping("/PDF")
public class PDFController {

    @Resource
    private JdbcTemplate jdbcTemplate;

    @RequestMapping(value = "/getPDF")
    public void getPDF(HttpServletResponse response, @RequestParam(name = "Stu_id") int Stu_id) throws JSONException, IOException {
        System.out.println("--------------------In getPDF Controller--------------------");
        String OutputPath = "D:\\softwave engineer\\中软实训\\project\\test-manager\\src\\main\\resources\\PDF\\Output.pdf";

        if(jdbcTemplate.queryForObject("select Stu_test_all from Student where Stu_id = "+Stu_id,String.class).equals("0"))
        {
            JSONObject ResultJSON = JSONUtil.CreateJSON(-1,"体检表未填写完毕",0,null);
            System.out.println("--------------------JSON--------------------\n"+ResultJSON);
            JSONUtil.JSONToResponse(response, ResultJSON);
        }

        String sql;
        String[] TablesName = {"Student", "Eye", "EBH", "Tooth", "Surgery", "Blood", "Internal", "Assay", "Chest", "Other", "Manage", "Boss"};
        Map<String, Object> EntitiesMap = new HashMap<>();
        for (String TableName : TablesName) {
            sql = "select * from " + TableName + " where Stu_id = " + Stu_id;
            EntitiesMap.putAll(jdbcTemplate.queryForMap(sql));
        }

        PDFHelper.fillPDFTemplate(PDFHelper.preProcess(EntitiesMap), OutputPath);
        response.reset();
        FileToWeb(response, "/PDF/Output.pdf", "Output.pdf");
        System.out.println("--------------------response--------------------\n"+response.getOutputStream().toString());
    }

    private void FileToWeb(HttpServletResponse response, String path, String fileName) throws IOException {
        response.setHeader("content-disposition", "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8"));
        response.setContentType("application/pdf; charset=utf-8");
        InputStream inputStream = this.getClass().getResourceAsStream(path);

        OutputStream outputStream = response.getOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        while (true) {
            assert inputStream != null;
            if ((len = inputStream.read(buffer)) == -1) break;
            outputStream.write(buffer, 0, len);
        }
        inputStream.close();
        outputStream.close();
    }

}
