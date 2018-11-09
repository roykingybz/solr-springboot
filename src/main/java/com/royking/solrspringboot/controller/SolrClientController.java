package com.royking.solrspringboot.controller;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class SolrClientController {

    @Autowired
    private SolrClient solrClient;

    /**
     * 给solr索引库添加or修改，ID存在时修改
     *
     * @param id
     * @param title
     * @return
     */
    @GetMapping("/add")
    public String addSolrIndex(String id, String title) {
        try {
            // 创建一个Document
            SolrInputDocument solrInputFields = new SolrInputDocument();
            solrInputFields.addField("id", id);
            solrInputFields.addField("title", title);
            solrInputFields.addField("content", "修改index内容");

            solrClient.add(solrInputFields);
            solrClient.commit();

            return "add index success";
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "error";
    }

    /**
     * 通过id删除相关索引
     * @param id
     * @return
     */
    @GetMapping("deleteIndexById")
    public String deleteSolrIndex(String id) {
        try{
          solrClient.deleteById(id);
          solrClient.commit();
          return "delete index success";
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "error";
    }

    /**
     * 删除所有solr中的index
     * @return
     */
    @GetMapping("deleteIndexAll")
    public String deleteSolrAll() {
        try {
            solrClient.deleteByQuery("*:*");
            solrClient.commit();
            return "delete all index success";
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "error";
    }

    /**
     * 根据ID查询index
     * @param id
     * @return
     */
    @GetMapping("selectById")
    public String selectById(String id) {
        try {
            SolrDocument document = solrClient.getById(id);
            Object index = document.get("id");
            Object title = document.get("title");
            Object content = document.get("content");
            System.out.println(index.toString());
            System.out.println(title.toString());
            System.out.println(content.toString());
            return document.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "error";
    }

    @GetMapping("search")
    public void search(){
        try{
            // 构建一个查询参数
            SolrQuery solrQuery = new SolrQuery();
            // 查询条件
            solrQuery.set("q","*:*");
            // 过滤条件
            solrQuery.set("fq","id:[1 TO 5]");
            // 排序
            solrQuery.addSort("id",SolrQuery.ORDER.asc);
            // 分页
            solrQuery.setStart(0);
            solrQuery.setRows(10);
            //默认域
            solrQuery.set("df", "title");
            //只查询指定域
            solrQuery.set("fl", "id,title,content");
            //高亮
            //打开开关
            solrQuery.setHighlight(true);
            //指定高亮域
            solrQuery.addHighlightField("title");
            //设置前缀
            solrQuery.setHighlightSimplePre("<span style='color:red'>");
            //设置后缀
            solrQuery.setHighlightSimplePost("</span>");

            QueryResponse queryResponse = solrClient.query(solrQuery);
            SolrDocumentList results = queryResponse.getResults();
            long numFound = results.getNumFound();
            System.out.println("numFound: " + numFound);
            // 获取高亮结果
            Map<String, Map<String, List<String>>> highlighting = queryResponse.getHighlighting();
            for (SolrDocument solrDocument : results) {
                System.out.println(solrDocument.get("id"));
                System.out.println(solrDocument.get("title"));
                System.out.println(solrDocument.get("content"));

                Map<String, List<String>> map = highlighting.get(solrDocument.get("id"));
                List<String> title = map.get("title");
                if (title != null && !title.isEmpty()) {
                    String titleStr = title.get(0);
                    System.out.println(titleStr);
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}
