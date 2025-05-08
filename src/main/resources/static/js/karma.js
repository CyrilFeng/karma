var account = "admin";

var oper_stack = [],
loop = {},
idMap = {},
vars = {},
instance,
editor,
editor_json,
editor_sql,
echart,
selected_node;
var tree = $.fn.zTree.init($("#main_tree_area_inner"), {
    edit: {
        enable: true,
        showRemoveBtn: false,
        showRenameBtn: false
    },
    async: {
        enable: true,
        url: "/api/dir/list",
        type: "get",
        autoParam: ["id"],
        otherParam: {
            "account": account
        }
    }
},
[{
    id: 0,
    pId: 0,
    name: "我的分析流",
    open: true,
    isParent: true
}]);

tree.setting.callback.onClick = function(event, treeId, treeNode) {
    if (!treeNode.isParent) {
        $.get("/api/analyseStream/get", {
            id: treeNode.id
        },
        function(response) {
            if (response.data) {
                $(".main_tool_title").text(response.data.name);
                $("#flow_id").val(response.data.id);
                $(".main_group_select").val(response.data.dirId);
                loadFlow(JSON.stringify(response.data.content));
            }
        });
    }
};

tree.setting.callback.onRename = function(event, treeId, treeNode) {
    if (!$.trim(treeNode.name)) return;
    $.ajax({
        url: "/api/dir/rename",
        type: "POST",
        async: false,
        contentType: "application/json",
        data: JSON.stringify({
            name: treeNode.name,
            id: treeNode.id
        })
    });
};

tree.setting.callback.onDblClick = function(event, treeId, treeNode) {
    if (treeNode.isParent) {
        tree.selectNode(treeNode);
        tree.editName(treeNode);
    }
};
$(".main_tree_tool_add").click(function() {
    $(".add_dir_name").show();
});

$(".tree_search").keyup(function(e) {
    var v = $.trim($(this).val());
    if (e.keyCode == 13) {
        tree.setting.async.otherParam.name = v;
        var rootNode = tree.getNodeByParam("id", 0, null);
        tree.reAsyncChildNodes(rootNode, 'refresh', false);
    }
});

$(".alert_btn").click(function() {
    $(".alert_tip").hide();
    if (!$(".main_ds").is(":visible")) {
        $(".main_modal").hide();
    }
    $(".alert_tip,.main_modal").css("z-index", "auto");
    if ($(".alert_tip").data().type == "main_tree_tool_del") {
        $.ajax({
            url: "/api/dir/delete",
            type: "POST",
            data: {
                id: $(".alert_tip").data().data
            },
            success: function(response) {
                var rootNode = tree.getNodeByParam("id", 0, null);
                tree.reAsyncChildNodes(rootNode, 'refresh', false);
                if (response.code < 0) {
                    $(".alert_tip,.main_modal").show();
                    $(".alert_text").text("只能删除空目录");
                    $(".alert_tip").data({
                        "type": ""
                    });
                }
            }
        });
    } else if ($(".alert_tip").data().type == "main_ds_table_head_col_del") {
        $.post("/api/dataSource/delete", {
            "id": ds_table_head_col_del.data().id
        },
        function() {
            ds_table_head_col_del.remove();
        });
    } else if ($(".alert_tip").data().type == "main_connect") {
        jsPlumb.getConnections().forEach(function(connection) {
            if (connection.sourceId == $(".alert_tip").data().data.source && connection.targetId == $(".alert_tip").data().data.target) {
                oper_stack.push({
                    "action": "deleteConn",
                    "data": {
                        "from": connection.sourceId,
                        "to": connection.targetId,
                        "anchor": [connection.endpoints[0].anchor.anchors ? connection.endpoints[0].anchor.anchors[0].type: connection.endpoints[0].anchor.type, connection.endpoints[1].anchor.anchors ? connection.endpoints[1].anchor.anchors[1].type: connection.endpoints[1].anchor.type]
                    }
                });
                jsPlumb.deleteConnection(connection);
            }
        });
    }
});

$(".alert_btn2").click(function() {
    $(".alert_tip,.main_modal").css("z-index", "auto");
    $(".alert_tip").hide();
    if (!$(".main_ds").is(":visible")) {
        $(".main_modal").hide();
    }
    $(".alert_tip").data({
        "type": ""
    });
});

$(".main_tree_tool_del").click(function() {
    var node = tree.getSelectedNodes()[0];
    if (node.id > 0) {
        $(".alert_tip").data({
            "type": "main_tree_tool_del",
            "data": node.id
        });
        $(".alert_tip,.main_modal").show();
        $(".alert_text").text("确定要删除【" + node.name + "】吗？");
    }
});

$(".tree_add_close").click(function() {
    $(".add_dir_name").hide();
    $(".tree_add").val("");
});

$(".tree_add").keyup(function(e) {
    var v = $.trim($(this).val());
    if (e.keyCode == 13 && v) {
        $.ajax({
            url: "/api/dir/save",
            type: "POST",
            async: false,
            contentType: "application/json",
            data: JSON.stringify({
                isParent: true,
                name: v,
                createUser: account,
                pId: 0
            }),
            success: function(response) {
                var rootNode = tree.getNodeByParam("id", 0, null);
                tree.reAsyncChildNodes(rootNode, 'refresh', false);
                $(".add_dir_name").hide();
                $(".tree_add").val("");
                loadDir();
            }
        });
    }
});

$(".main_tool_save").click(function() {
    var valid = true;
    $.ajax({
        url: "/api/analyseStream/check",
        type: "POST",
        async: false,
        contentType: "application/json",
        data: JSON.stringify(getResult()),
        success: function(response) {
            $(".main_console_area").empty();
            for (var i = 0; i < response.data.length; i++) {
                valid = false;
                $(".main_console_area").append("<div style='color:#D62B34'>" + response.data[i] + "</div>");
            }
        }
    });
    if (!valid) return;

    var saveReq = {
        "name": $.trim($(".main_tool_title").text()),
        "dirId": $(".main_group_select").val(),
        "createUser": account,
        "content": getResult()
    };
    if ($("#flow_id").val()) {
        saveReq.id = $("#flow_id").val();
    }

    $.ajax({
        url: "/api/analyseStream/save",
        type: "POST",
        async: false,
        contentType: "application/json",
        data: JSON.stringify(saveReq),
        success: function(response) {
            $(".main_console_area").append("<div style='color:#3B8D42'>保存成功</div>");
            $("#flow_id").val(response.data);
            var rootNode = tree.getNodeByParam("id", 0, null);
            tree.reAsyncChildNodes(rootNode, 'refresh', false);
        }
    });
});

function getCnNameFromParams(dsParams, name) {
    for (var i = 0; i < dsParams.length; i++) {
        if ("a_" + dsParams[i].name == name && $.trim(dsParams[i].cn_name)) {
            return dsParams[i].cn_name;
        }
    }
    return name;
}

function resultLoop(id, name, dsParams, next) {
    var t = setInterval(function() {
        $.ajax({
            url: "/api/analyseStream/result/query?nextUri=" + next,
            type: "GET",
            success: function(response) {
                if (response.code < 0) {
                    $(".main_console_area").append("<div style='color:#D62B34'>" + response.msg + "</div>");
                } else if (response.data.error) {
                    $(".main_console_area").append("<div style='color:#D62B34'>" + formatDateTime(new Date(), 'yyyy-MM-dd HH:mm:ss S') + " " + name + "[" + id + "] &nbsp;&nbsp;&nbsp; "+ response.data.error.message + "</div>");
                    loop[t] = "error";
                    clearInterval(t);
                    delete loop[t];
                } else {
                    $(".main_console_area").append("<div style='color:#fff'>" + formatDateTime(new Date(), 'yyyy-MM-dd HH:mm:ss S') + " " + name + "[" + id + "] &nbsp;&nbsp;&nbsp;状态:" + response.data.stats.state + " &nbsp;&nbsp;&nbsp;进度:" + (response.data.stats.progressPercentage ? response.data.stats.progressPercentage.toFixed(2): 0) + "% </div>");
                    next = response.data.nextUri;
                    loop[t] = next;
                    if (response.data.data) {
                        clearInterval(t);
                        delete loop[t];
                        $(".main_console_area").append("<div style='color:#fff'>" + name + "[" + id + "] &nbsp;&nbsp;&nbsp;" + response.data.id + " &nbsp;&nbsp;&nbsp;执行完成</div>");
                        var o = $("#" + idMap[response.data.id]).children(".node_result");
                        o.empty();
                        for (var i = 0; i < response.data.columns.length; i++) {
                            o.append('<div class="node_result_row"><div class="node_result_row_key">' + getCnNameFromParams(dsParams, response.data.columns[i].name) + '</div><div class="node_result_row_val">' + response.data.data[0][i] + '</div></div>');
                        }
                        o.slideDown(1000);
                          var row1 = o.find(".node_result_row");
                                                var conn = jsPlumb.getConnections();
                                                for (var i = 0; i < conn.length; i++) {
                                                   var label="";
                                                   if(conn[i].sourceId==o.parent().attr("id")){
                                                    for(var j=0;j<row1.length;j++){
                                                      var k = $(row1[j]).children(".node_result_row_key").text();
                                                      var v = $(row1[j]).children(".node_result_row_val").text();
                                                      var v2 = $("#" + conn[i].targetId).find(".node_result_row_key").filter(function() {
                                                           return $(this).text().trim() == k;
                                                      }).next().text();
                                                      if(v2){
                                                      label+=k+"转化："+(v2/v*100).toFixed(2)+"%<br>";
                                                      }
                                                    }
                                                   }else if(conn[i].targetId==o.parent().attr("id")){
                                                       for(var j=0;j<row1.length;j++){
                                                        var k = $(row1[j]).children(".node_result_row_key").text();
                                                        var v = $(row1[j]).children(".node_result_row_val").text();
                                                        var v2 = $("#" + conn[i].sourceId).find(".node_result_row_key").filter(function() {
                                                           return $(this).text().trim() == k;
                                                        }).next().text();
                                                         if(v2){
                                                           label+=k+"转化："+(v/v2*100).toFixed(2)+"%<br>";
                                                         }
                                                       }
                                                    }
                                                   if(label&&!hasLabelOverlay(conn[i])){
                                                       conn[i].addOverlay(["Label", {
                                                              label: label,
                                                              location: 0.7,
                                                              cssClass: "jsplumb_label"
                                                            }]);
                                                   }
                                                }
                    }
                }
                $(".main_console_area").scrollTop($(".main_console_area").height() + $(".main_console_area").scrollTop());
            }
        });
    },
    6000);
    loop[t] = next;
}


function hasLabelOverlay(connection) {
  var overlays = connection.getOverlays();
  for (var overlayId in overlays) {
    if (overlays[overlayId].type === "Label") {
      return true;
    }
  }
  return false;
}


function clearLabel(){
  jsPlumb.getAllConnections().forEach(function(conn) {
      var overlays = conn.getOverlays();
      for (var id in overlays) {
        if (overlays[id].type == "Label") {
          conn.removeOverlay(id);
        }
      }
    });
}


$(".main_tool_stop").click(function() {
    for (var k in loop) {
        $.ajax({
            url: "/api/analyseStream/terminate",
            type: "POST",
            async: false,
            data: {
                "nextUri": loop[k]
            },
            success: function() {
                $(".main_console_area").append("<div style='color:#fff'>任务 " + loop[k] + " &nbsp;&nbsp;&nbsp;已终止</div>");
                $(".main_console_area").scrollTop($(".main_console_area").height() + $(".main_console_area").scrollTop());
            }
        });
        clearInterval(k);
        delete loop[k];
    }
    idMap = {};
        $(".user_node,.fellow_node").children(".node_result").hide();
        $(".user_node,.fellow_node").children(".node_result").empty();
    clearLabel();
});

$(".main_menu_item_run").click(function() {
    runFlow(selected_node);
});

$(".main_tool_run").click(runFlow);

function runFlow(node) {
    if (!$.isEmptyObject(loop)) return;
    idMap = {};
    $(".user_node,.fellow_node").children(".node_result").hide();
    clearLabel();
    var result = getResult();
    var dsAll = [],
    dsConfig = {};
    for (var k in result.nodes) {
        dsAll.push(result.nodes[k].ds);
    }

    if (dsAll.length > 0) {
        var dsCodesStr = dsAll.join(',');
        $.ajax({
            url: "/api/dataSource/queryByCodes?dsCodes=" + dsCodesStr,
            type: "GET",
            async: false,
            success: function(response) {
                dsConfig = response.data;
            }
        });
    }

    var request = {
        "content": result,
        "account": account
    };

    if (node.attr) {
        request.nodeCode = node.attr("id");
    }

    $.ajax({
        url: "/api/analyseStream/execute",
        type: "POST",
        async: false,
        contentType: "application/json",
        data: JSON.stringify(request),
        success: function(response) {
            $(".main_console_area").empty();
            if (response.code < 0) {
                $(".main_console_area").append("<div style='color:#D62B34'>" + response.msg + "</div>");
            } else {
                for (var k in response.data) {
                    $(".main_console_area").append("<div style='color:#fff'>" + formatDateTime(new Date(), 'yyyy-MM-dd HH:mm:ss S') + "  " + result.nodes[k].name + "[" + k + "] &nbsp;&nbsp;&nbsp;<a target='_blank' href=" + response.data[k].infoUri + ">" + response.data[k].infoUri + "</a></div>");
                    resultLoop(k, result.nodes[k].name, JSON.parse(dsConfig[result.nodes[k].ds].dsParams), response.data[k].nextUri);
                    idMap[response.data[k].id] = k;
                }
                $(".main_console_area").scrollTop($(".main_console_area").height() + $(".main_console_area").scrollTop());
            }
        }
    });
}

function init_editor_json() {
    editor_json = CodeMirror($(".main_conf_base_code")[0], {
        lineNumbers: true,
        mode: "javascript",
        theme: 'monokai'
    });
    editor_json.setSize(740, 530);
}

function init_editor_sql() {
    editor_sql = CodeMirror($(".main_conf_base_code_sql")[0], {
        lineNumbers: true,
        mode: "sql",
        theme: 'monokai',
        readOnly: true
    });
    editor_sql.setSize(740, 570);
}

function init_editor() {
    editor = CodeMirror($(".main_ds_edit_sql")[0], {
        lineNumbers: true,
        mode: "sql",
        theme: 'monokai'
    });

    editor.on('change', (ins) =>{
        var v = $.trim(ins.getValue());
        var arr = extractVariables(v);
        var cols = getColumnNamesFromSQL(v);
        var rows = $(".main_ds_edit_params_body>.main_ds_table_row");
        var c_rows = $(".main_ds_edit_cols_body>.main_ds_table_row");
        var oldVals = [];
        rows.each(function() {
            oldVals.push($.trim($(this).find("div:first").text()));
        });
        var c_oldVals = [];
        c_rows.each(function() {
            c_oldVals.push($.trim($(this).find("div:first").text()));
        });

        difference(oldVals, arr).forEach(function(item) {
            $("#var_" + item).remove();
        });
        difference(c_oldVals, cols).forEach(function(item) {
            $("#col_" + item).remove();
        });
        rows = $(".main_ds_edit_params_body>.main_ds_table_row");
        c_rows = $(".main_ds_edit_cols_body>.main_ds_table_row");

        for (var i = 0; i < cols.length; i++) {
            var distinct = false;
            c_rows.each(function() {
                if ($.trim($(this).find("div:first").text()).indexOf(cols[i]) == 0) {
                    distinct = true;
                    return false;
                }
            });

            if (distinct) continue;
            var s = '<div class="main_ds_table_row" id="col_' + cols[i] + '">';
            s += '<div class="main_ds_table_head_col"  style="width:120px;">' + cols[i] + '</div>';
            s += '<div class="main_ds_table_head_col" style="width:120px;"><input type="text" class="main_dynamic_input"></div>';
            s += '<div class="main_ds_table_head_col" style="width:120px;"><select style="height:26px" class="main_dynamic_input"><option value="text">文本</option><option value="number">数字</option><option value="date">日期</option><option value="datetime">日期时间</option></select></div>';
            s += '<div class="main_ds_table_head_col" style="width:180px;"><input type="text" class="main_dynamic_input"></div> </div>';
            $(".main_ds_edit_cols_body").append(s);
        }

        for (var i = 0; i < arr.length; i++) {
            var distinct = false;
            rows.each(function() {
                if ($.trim($(this).find("div:first").text()).indexOf(arr[i]) == 0) {
                    distinct = true;
                    return false;
                }
            });

            if (distinct) continue;
            var s = '<div class="main_ds_table_row" id="var_' + arr[i] + '">';
            s += '<div class="main_ds_table_head_col"  style="width:120px;">' + arr[i] + '</div>';
            s += '<div class="main_ds_table_head_col" style="width:120px;"><input type="text" class="main_dynamic_input"></div>';
            s += '<div class="main_ds_table_head_col" style="width:120px;"><select style="height:26px" class="main_dynamic_input"><option value="text">文本</option><option value="number">数字</option><option value="date">日期</option><option value="datetime">日期时间</option></select></div>';
            s += '<div class="main_ds_table_head_col" style="width:180px;"><input type="text" class="main_dynamic_input"></div> </div>';
            $(".main_ds_edit_params_body").append(s);
        }
    });
}

$(".main_var_tool_add").click(function(){
   var s = '<div class="main_var_table_row">';
       s += '<div class="main_var_table_head_col"  style="width:110px;"> <input type="text" class="main_dynamic_input"></div>';
       s += '<div class="main_var_table_head_col" style="width:110px;"><input type="text" class="main_dynamic_input"></div>';
       s += '<div class="main_var_table_head_col" style="width:50px;"><div class="main_var_table_head_col_del" style="margin-left:8px;"></div></div> </div>';
       $(".main_var_table_body").append(s);
});

$(document).on("click",".main_var_table_head_col_del",function(){
   $(this).parents(".main_var_table_row").remove();
});

$(".main_var .main_submit").click(function(){
   vars={};
   $(".main_var_table_row").each(function(){
      var k = $.trim($(this).find(".main_dynamic_input:first").val());
      var v = $.trim($(this).find(".main_dynamic_input:last").val());
      if(k&&v){
         vars[k]=v;
      }
   });
   $(".main_var_table_body").empty();
   $(".main_var,.main_modal").hide();
});

function loadDir() {
    $.get("/api/dir/list", {
        account: account,
        id: 0
    },
    function(r) {
        $(".main_group_select").empty();
        $(".main_group_select").append('<option value="0">我的分析流</option>');
        for (var i = 0; i < r.length; i++) {
            if (r[i].isParent) {
                $(".main_group_select").append("<option value='" + r[i].id + "'>" + r[i].name + "</option>");
            }
        }
    });
}

loadDir();

$(".ds_search").keyup(function(e) {
    if (e.keyCode == 13) {
        loadDS();
    }
});

function extractVariables(sql) {
    const regex = /\$\{(\w+)\}/g;
    let match;
    const variables = [];
    while ((match = regex.exec(sql)) !== null) {
        variables.push(match[1]);
    }
    return variables;
}

function difference(arr1, arr2) {
    const set2 = new Set(arr2);
    return arr1.filter(function(item) {
        return ! set2.has(item);
    });
}

$(".main_menu_item_sql").click(function() {
    if (!selected_node) return;
    $.ajax({
        url: "/api/analyseStream/generateTaskSql?nodeId=" + selected_node.attr("id"),
        type: "POST",
        async: false,
        contentType: "application/json",
        data: JSON.stringify(getResult()),
        success: function(response) {
            if (response.data && response.data[selected_node.attr("id")]) {
                $(".main_code_sql,.main_modal").show();
                if (!editor_sql) {
                    init_editor_sql();
                }
                editor_sql.setValue(sqlFormatter.format(response.data[selected_node.attr("id")],{ language: 'trino' }));
            } else {
                $(".main_console_area").empty();
                $(".main_console_area").append("<div style='color:#D62B34'>" + response.msg + "</div>");
            }
        }
    });
});

$(".main_tool_code").click(function() {
    $(".main_code,.main_modal").show();
    if (!editor_json) {
        init_editor_json();
    }
    editor_json.setValue(JSON.stringify(getResult(), null, 4));
});

$(".main_ds_tool_add").click(function() {
    $(".main_ds_edit_panel").show();
    if (!editor) {
        init_editor();
    }
    editor.setValue("");
    $(".main_ds_edit_panel .main_ds_edit_code,.main_ds_edit_panel .main_ds_edit_name,.main_ds_edit_panel .main_ds_edit_sql").removeClass("m_error");
    $(".main_ds_edit_panel .main_ds_edit_id,.main_ds_edit_panel .main_ds_edit_code,.main_ds_edit_panel .main_ds_edit_name").val("");
    $(".main_ds_edit_panel .main_ds_select").val("user");
    $(".main_ds_edit_panel .main_ds_edit_params_body,.main_ds_edit_panel .main_ds_edit_cols_body").empty();
    $(".main_ds_edit_panel .main_ds_edit_head_title").text("新增数据源");
});

$(".main_ds_head_close").click(function() {
    $(".main_modal,.main_ds,.main_ds_edit_panel").hide();
});

$(".main_code_head_close").click(function() {
    $(".main_modal,.main_code").hide();
});

$(".main_code_sql_head_close").click(function() {
    $(".main_modal,.main_code_sql").hide();
});

$(".main_ds_edit_head_close").click(function() {
    $(".main_ds_edit_panel").hide();
});

function loadDS() {
    $.get("/api/dataSource/list", {
        "queryStr": $.trim($(".ds_search").val())
    },
    function(r) {
        $(".main_ds_table_body").empty();
        for (var i = 0; i < r.data.length; i++) {
            var o = '     <div class="main_ds_table_row">';
            o += ' <div class="main_ds_table_head_col" style="width:150px;">';
            o += r.data[i].dsCode;
            o += '</div>';
            o += '<div class="main_ds_table_head_col" style="width:150px;">';
            o += r.data[i].dsName;
            o += '</div>';
            o += '<div class="main_ds_table_head_col">';
            o += r.data[i].dsType == 'user' ? '用户': '销售';
            o += '</div>';
            o += '<div class="main_ds_table_head_col">';
            o += r.data[i].createUser;
            o += '</div>';
            o += '<div class="main_ds_table_head_col" style="width:150px;">';
            o += r.data[i].createTime.replace("T", " ");
            o += '</div>';
            o += '<div class="main_ds_table_head_col">';
            o += '<div class="main_ds_table_head_col_edit">';
            o += '</div>';
            o += '<div class="main_ds_table_head_col_del">';
            o += '</div>';
            o += '</div>';
            o += '</div>';
            $(".main_ds_table_body").append(o);
            $(".main_ds_table_body>div:last").data(r.data[i]);
        }
    });
}

$(".main_menu_ds").click(function() {
    $(".main_modal,.main_ds").show();
    $(".ds_search").val("");
    loadDS();
});

$(".main_conf_base_type_ds_open").click(function() {
    if (!selected_node) return;
    $(".main_modal,.main_ds").show();
    $(".ds_search").val("");
    loadDS();
});

$(".main_conf_base_rela_radio").click(function() {
    if (!selected_node) return;
    selected_node.data().relation = $(this).attr("data");
});

$(".main_conf_base_type_desc").keyup(function() {
    if (!selected_node) return;
    selected_node.data().desc = $.trim($(this).val());
});

$(document).on("keyup", ".main_conf_param_outer .param_v_input",
function() {
    if (!selected_node) return;
    var v = $.trim($(this).val());
    selected_node.data().props[$(this).parent().parent().data().name] = v;
});

$(document).on("change", ".main_conf_aggr_outer .param_v_input",
function() {
    if (!selected_node) return;
    var v = $(this).val();
    selected_node.data().aggrs[$(this).parent().parent().data().name] = v;
});

$(document).on("dblclick", ".main_ds .main_ds_table_row",
function() {
    if (!selected_node) return;
    $(".main_conf_base_type_ds").val($(this).data().dsName);
    selected_node.data().ds_cn_name = $(this).data().dsName;
    selected_node.data().ds = $(this).data().dsCode;
    selected_node.data().aggrs = {};
    selected_node.data().props = {};
    $(".main_ds,.main_modal").hide();
    $(".main_conf_param_outer").empty();
    $.get("/api/dataSource/queryByCode", {
        "dsCode": $(this).data().dsCode
    },
    function(data) {
        var dsProps = JSON.parse(data.data.dsProps);
        for (var i = 0; i < dsProps.length; i++) {
            var o = '<div class="param_row"><div class="param_k">' + (dsProps[i].cn_name ? dsProps[i].cn_name: dsProps[i].name) + '</div><div class="param_v"><input type="text" class="param_v_input"></div></div>';
            $(".main_conf_param_outer").append(o);
            $(".main_conf_param_outer>.param_row:last").data(dsProps[i]);
        }
    });
    genParams(selected_node.attr("id"));
});

$(".main_tool_close").click(function() {
    $(".main_tool,.main_canvas,.main_left_tool,.main_conf,.main_tree,.main_console,.main_chart_inner").hide();
    clearRight();
    clearFlow();
    if(echart){
      echart.clear();
    }
    $(".main_tool_title").text("新建分析流");
    $("#flow_id").val("");
    $(".main_group_select").val("0");
    $(".main_console_area").empty();
    $(".main_banner").fadeIn(1000,
    function() {
        $(".main_start,.copyright_info").fadeIn(500);
    });
});

$(".main_code .main_submit").click(function() {
    loadFlow(editor_json.getValue());
});

function clearFlow() {
    jsPlumb.getConnections().forEach(function(connection) {
        jsPlumb.deleteConnection(connection);
    });
    jsPlumb.deleteEveryEndpoint();
    $(".main_canvas_inner_scroll").empty();
    oper_stack = [];
}

function loadFlow(json) {
    if (!json) return;
    clearFlow();
    obj = JSON.parse(json.replace(/\n/g, ""));
    node_cnt = 1;
    vars = obj.vars?obj.vars:{};
    for (var node_id in obj.nodes) {
        var curId = parseInt(node_id.split("_")[1]);
        if (node_cnt < curId) {
            node_cnt = curId;
        }
        var o;
        if (obj.nodes[node_id].type == "fellow") {
            o = $("<div id='" + node_id + "' class='fellow_node'><div>" + obj.nodes[node_id].name + "</div><div class='node_result'></div></div>");
            o.data({
                "type": "fellow",
                "desc": obj.nodes[node_id].desc,
                "relation": obj.nodes[node_id].relation,
                "ds": obj.nodes[node_id].ds,
                "ds_cn_name": obj.nodes[node_id].ds_cn_name,
                "props": obj.nodes[node_id].props,
                "aggrs": obj.nodes[node_id].aggrs
            });
        } else if (obj.nodes[node_id].type == "user") {
            o = $("<div id='" + node_id + "' class='user_node'><div>" + obj.nodes[node_id].name + "</div><div class='node_result'></div></div>");
            o.data({
                "type": "user",
                "desc": obj.nodes[node_id].desc,
                "relation": obj.nodes[node_id].relation,
                "ds": obj.nodes[node_id].ds,
                "ds_cn_name": obj.nodes[node_id].ds_cn_name,
                "props": obj.nodes[node_id].props,
                "aggrs": obj.nodes[node_id].aggrs
            });
        }
        o.css("top", obj.nodes[node_id].top);
        o.css("left", obj.nodes[node_id].left);
        $(".main_canvas_inner_scroll").append(o);
        initNode(node_id);
    }

    for (var i = 0; i < obj.lines.length; i++) {
        jsPlumb.connect({
            source: obj.lines[i].from,
            target: obj.lines[i].to,
            maxConnections: -1,
            endpoint: "Blank",
            anchor: obj.lines[i].anchor
        });
    }

    node_cnt++;
    $(".main_code,.main_modal").hide();
    $(".user_node,.fellow_node").removeClass("selected_node");
    selected_node = null;
    clearRight();
}

function clearRight() {
    $(".main_conf_base_type_name").val("");
    $(".main_conf_base_type_input").val("");
    $(".main_conf_base_type_desc").val("");
    $(".main_conf_base_type_ds").val("");
    $(".main_conf_base_rela_radio").removeClass("main_conf_base_rela_radio_checked");
    $(".main_conf_base_rela_radio[data='intersection']").addClass("main_conf_base_rela_radio_checked");
    $(".main_conf_param_outer,.main_conf_aggr_outer").empty();
}

$(".main_ds_edit_panel .main_submit").click(function() {
    var code = $.trim($(".main_ds_edit_panel .main_ds_edit_code").val());
    var name = $.trim($(".main_ds_edit_panel .main_ds_edit_name").val());
    var type = $(".main_ds_edit_panel .main_ds_select").val();
    var sql = $.trim(editor.getValue());
    var ds_props = [];
    var ds_params = [];
    var valid = true;
    if (!code) {
        $(".main_ds_edit_panel .main_ds_edit_code").addClass("m_error");
        valid = false;
    }
    if (!name) {
        $(".main_ds_edit_panel .main_ds_edit_name").addClass("m_error");
        valid = false;
    }
    if (!sql) {
        $(".main_ds_edit_panel .main_ds_edit_sql").addClass("m_error");
        valid = false;
    }

    if (!valid) {
        return;
    }

    $(".main_ds_edit_panel .main_ds_edit_params_body>.main_ds_table_row").each(function() {
        var o = {
            "fmt": $(this).children(":eq(3)").children("input").val(),
            "type": $(this).children(":eq(2)").children("select").val(),
            "name": $(this).children(":eq(0)").text(),
            "cn_name": $(this).children(":eq(1)").children("input").val()
        }
        ds_props.push(o);
    });

    var valid_uid = false;

    $(".main_ds_edit_panel .main_ds_edit_cols_body>.main_ds_table_row").each(function() {
        if ($(this).children(":eq(0)").text() == "uid") valid_uid = true;
        var o = {
            "fmt": $(this).children(":eq(3)").children("input").val(),
            "type": $(this).children(":eq(2)").children("select").val(),
            "name": $(this).children(":eq(0)").text(),
            "cn_name": $(this).children(":eq(1)").children("input").val()
        }
        ds_params.push(o);
    });

    if (!valid_uid) {
        $(".alert_tip,.main_modal").show();
        $(".main_modal,.alert_tip").css("z-index", 999);
        $(".alert_text").text("SQL语句中必须包含uid");
        return;
    }

    var data = {
        "createUser": account,
        "dsParams": JSON.stringify(ds_params),
        "dsProps": JSON.stringify(ds_props),
        "dsSql":  sqlFormatter.format(sql,{ language: 'trino' }),
        "dsName": name,
        "dsCode": code,
        "dsType": type
    };
    var id = $(".main_ds_edit_id").val();
    if (id) {
        data.id = id;
    }

    $.ajax({
        url: "/api/dataSource/save",
        type: "POST",
        async: false,
        contentType: "application/json",
        data: JSON.stringify(data),
        success: function(response) {
            $(".main_ds_edit_panel").hide();
            loadDS();
        },
        error: function(xhr, status, e) {
            $(".alert_tip,.main_modal").show();
            $(".main_modal,.alert_tip").css("z-index", 999);
            $(".alert_text").text("数据源编码不能重复");
        }
    });
});

var ds_table_head_col_del;
$(document).on("click", ".main_ds_table_body .main_ds_table_head_col_del",
function() {
    ds_table_head_col_del = $(this).parent().parent();
    $(".alert_tip").data({
        "type": "main_ds_table_head_col_del"
    });
    $(".alert_tip,.main_modal").show();
    $(".main_modal,.alert_tip").css("z-index", 999);
    $(".alert_text").text("确定要删除【" + $(this).parent().parent().children(".main_ds_table_head_col:eq(1)").text() + "】吗？");
});

$(document).on("click", ".main_ds_table_body .main_ds_table_head_col_edit",
function() {
    $(".main_ds_edit_panel").show();
    if (!editor) {
        init_editor();
    }
    $(".main_ds_edit_panel .main_ds_edit_code,.main_ds_edit_panel .main_ds_edit_name,.main_ds_edit_panel .main_ds_edit_sql").removeClass("m_error");
    $(".main_ds_edit_panel .main_ds_edit_head_title").text("编辑数据源");
    editor.setValue($(this).parent().parent().data().dsSql);
    $(".main_ds_edit_panel .main_ds_edit_id").val($(this).parent().parent().data().id);
    $(".main_ds_edit_panel .main_ds_edit_code").val($(this).parent().parent().data().dsCode);
    $(".main_ds_edit_panel .main_ds_edit_name").val($(this).parent().parent().data().dsName);
    $(".main_ds_edit_panel .main_ds_select").val($(this).parent().parent().data().dsType);
    $(".main_ds_edit_panel .main_ds_edit_params_body,.main_ds_edit_panel .main_ds_edit_cols_body").empty();

    var dsParams = JSON.parse($(this).parent().parent().data().dsParams);
    var dsProps = JSON.parse($(this).parent().parent().data().dsProps);

    for (var i = 0; i < dsProps.length; i++) {
        var s = '<div class="main_ds_table_row" id="var_' + dsProps[i].name + '">';
        s += '<div class="main_ds_table_head_col"  style="width:120px;">' + dsProps[i].name + '</div>';
        s += '<div class="main_ds_table_head_col" style="width:120px;"><input type="text" class="main_dynamic_input" value="' + dsProps[i].cn_name + '"></div>';
        s += '<div class="main_ds_table_head_col" style="width:120px;"><select style="height:26px" class="main_dynamic_input"><option value="text" ' + (dsProps[i].type == "text" ? "selected": "") + '>文本</option><option value="number" ' + (dsProps[i].type == "number" ? "selected": "") + '>数字</option><option value="date" ' + (dsProps[i].type == "date" ? "selected": "") + '>日期</option><option value="datetime" ' + (dsProps[i].type == "datetime" ? "selected": "") + '>日期时间</option></select></div>';
        s += '<div class="main_ds_table_head_col" style="width:180px;"><input type="text" class="main_dynamic_input" value="' + dsProps[i].fmt + '"></div> </div>';
        $(".main_ds_edit_panel .main_ds_edit_params_body").append(s);
    }

    for (var i = 0; i < dsParams.length; i++) {
        var s = '<div class="main_ds_table_row" id="col_' + dsParams[i].name + '">';
        s += '<div class="main_ds_table_head_col"  style="width:120px;">' + dsParams[i].name + '</div>';
        s += '<div class="main_ds_table_head_col" style="width:120px;"><input type="text" class="main_dynamic_input" value="' + dsParams[i].cn_name + '"></div>';
        s += '<div class="main_ds_table_head_col" style="width:120px;"><select style="height:26px" class="main_dynamic_input"><option value="text" ' + (dsParams[i].type == "text" ? "selected": "") + '>文本</option><option value="number" ' + (dsParams[i].type == "number" ? "selected": "") + '>数字</option><option value="date" ' + (dsParams[i].type == "date" ? "selected": "") + '>日期</option><option value="datetime" ' + (dsParams[i].type == "datetime" ? "selected": "") + '>日期时间</option></select></div>';
        s += '<div class="main_ds_table_head_col" style="width:180px;"><input type="text" class="main_dynamic_input" value="' + dsParams[i].fmt + '"></div> </div>';
        $(".main_ds_edit_panel .main_ds_edit_cols_body").append(s);
    }

});

$(".main_tool_stop").click(function() {
    if (instance) {
        clearInterval(instance);
        clearLabel();
    }
});

$(".main_menu_item_del").click(function() {
    if (selected_node) {
        oper_stack.push({
            "action": "removeNode",
            "data": selected_node.attr("id"),
            "name": selected_node.children("div:first").text(),
            "top": selected_node.css("top"),
            "left": selected_node.css("left"),
            "info": JSON.parse(JSON.stringify(selected_node.data()))
        });
        jsPlumb.remove(selected_node.attr("id"));
        selected_node = null;
    }
});

$(".main_conf_base_rela_radio").click(function() {
    $(".main_conf_base_rela_radio").removeClass("main_conf_base_rela_radio_checked");
    $(this).addClass("main_conf_base_rela_radio_checked");
});

$(document).click(function() {
    $(".main_menu").hide();
});

$(".main_canvas").on("contextmenu",
function(e) {
    e.preventDefault();
    if (e.target.className == "main_canvas_inner_scroll" || !e.target.className) {
        $(".main_menu").css("top", e.clientY - 8);
        $(".main_menu").css("left", e.clientX - 8);
        $(".main_menu").show();
    }
});

$(".main_left_tool>div").click(function() {
    $(".main_left_tool>div").removeClass("selected_tool");
    $(this).addClass("selected_tool");
});

$(".main_tool_title_input").blur(function() {
    $(".main_tool_title").show();
    $(".main_tool_title_input").hide();
    $(".main_tool_title").text($(".main_tool_title_input").val());
});

$(".main_tool_edit_title").click(function() {
    if ($(".main_tool_title_input").is(":visible")) {
        $(".main_tool_title").show();
        $(".main_tool_title_input").hide();
        $(".main_tool_title").text($(".main_tool_title_input").val());
    } else {
        $(".main_tool_title").hide();
        $(".main_tool_title_input").show();
        $(".main_tool_title_input").val($(".main_tool_title").text());
    }
});

$(".main_console_tool_del").click(function() {
    $(".main_console_area").empty();
});

function getResult() {
    var r = {
        "nodes": {},
        "lines": [],
        "vars":vars
    };
    $(".user_node").each(function() {
        r.nodes[$(this).attr("id")] = {
            "name": $(this).children("div:first").text(),
            "type": "user",
            "desc": $(this).data().desc,
            "relation": $(this).data().relation,
            "ds": $(this).data().ds,
            "top": $(this).css("top"),
            "left": $(this).css("left"),
            "ds_cn_name": $(this).data().ds_cn_name,
            "aggrs": $(this).data().aggrs,
            "props": $(this).data().props
        };
    });
    $(".fellow_node").each(function() {
        r.nodes[$(this).attr("id")] = {
            "name": $(this).children("div:first").text(),
            "type": "fellow",
            "desc": $(this).data().desc,
            "relation": $(this).data().relation,
            "ds": $(this).data().ds,
            "top": $(this).css("top"),
            "left": $(this).css("left"),
            "ds_cn_name": $(this).data().ds_cn_name,
            "aggrs": $(this).data().aggrs,
            "props": $(this).data().props
        };
    });
    var conn = jsPlumb.getConnections();
    for (var i = 0; i < conn.length; i++) {
        r.lines.push({
            "from": conn[i].sourceId,
            "to": conn[i].targetId,
            "anchor": [conn[i].endpoints[0].anchor.anchors ? conn[i].endpoints[0].anchor.anchors[0].type: conn[i].endpoints[0].anchor.type, conn[i].endpoints[1].anchor.anchors ? conn[i].endpoints[1].anchor.anchors[1].type: conn[i].endpoints[1].anchor.type]
        });
    }
    return r;
}

$(document).on("click", ".user_node",function() {
    focusNode($(this));
});

$(".main_chart_head_close").click(function(){
    $(".main_chart_inner,.main_chart_head_close").hide();
    echart.clear();
});

$(".main_tool_graph").click(function(){
 var nodeIds = [];
 $(".selected_node").each(function(){
     nodeIds.push($(this).attr("id"));
 });
 if(nodeIds.length<2)  {
     $(".main_console_area").append("<div style='color:#D62B34'>请选择至少2个节点(按住ctrl或者command键多选)</div>");
     $(".main_console_area").scrollTop($(".main_console_area").height() + $(".main_console_area").scrollTop());
     return;
 }
 if($(".selected_node>.node_result:visible").length==0){
     $(".main_console_area").append("<div style='color:#D62B34'>请先运行分析流</div>");
     $(".main_console_area").scrollTop($(".main_console_area").height() + $(".main_console_area").scrollTop());
     return;
 }

  var data=[];
  $(".selected_node>.node_result:visible").each(function(){
      data.push({name:$(this).prev().text()+"-"+$(this).children(".node_result_row:first").children(".node_result_row_key").text(),value:$(this).children(".node_result_row:first").children(".node_result_row_val").text()});
  });

  $.ajax({
         url: "/api/analyseStream/checkRelations?nodeIds=" + nodeIds.join(","),
         type: "POST",
         async: false,
         contentType: "application/json",
         data: JSON.stringify(getResult()),
         success: function(response) {
           if(response.data=="SAME_PATH"){
               $(".main_chart_inner,.main_chart_head_close").show();
               if(!echart){
                  echart = echarts.init($(".main_chart_inner")[0]);
               }
               var option = {
                      series: [
                       {
                         name: 'Funnel',
                         type: 'funnel',
                         left: 'center',
                         top: 60,
                         bottom: 60,
                         width: '50%',
                         label: {
                           show: true,
                           position: 'inside',
                           formatter: '{b}:{c}'
                         },
                         data:data
                       }
                     ]
                   };
                   echart.setOption(option);
           }else if(response.data=="SAME_LEVEL"){
               $(".main_chart_inner,.main_chart_head_close").show();
               if(!echart){
                  echart = echarts.init($(".main_chart_inner")[0]);
               }
               var option = {
                 series: [
                   {
                     type: 'pie',
                     radius: '55%',
                     center: ['50%', '50%'],
                     data:data.sort(function (a, b) {
                       return a.value - b.value;
                     }),
                     label: {
                       show: true,
                       position: 'inside',
                       formatter: '{b}:{c}'
                     }
                   }
                 ]
               };
               echart.setOption(option);
           }else{
               $(".main_console_area").append("<div style='color:#D62B34'>您选择的节点无法构建饼图或者漏斗，请确保它们在同一路径或者同一层级</div>");
               $(".main_console_area").scrollTop($(".main_console_area").height() + $(".main_console_area").scrollTop());
           }
         }
  });
});

function focusNode(obj) {
    if(command&&obj.hasClass("selected_node")){
      clearRight();
      obj.removeClass("selected_node");
      selected_node = null;
      return;
    }
    $(".main_left_tool>div").removeClass("selected_tool");
    $(".main_left_tool_select").addClass("selected_tool");
    if(!command){
      $(".user_node,.fellow_node").removeClass("selected_node");
    }
    obj.addClass("selected_node");
    selected_node = obj;
    $(".main_conf_base_type_name").val(selected_node.children("div:first").text());
    $(".main_conf_base_type_input").val("用户节点");
    $(".main_conf_base_type_desc").val(selected_node.data().desc);
    $(".main_conf_base_type_ds").val(selected_node.data().ds_cn_name);
    $(".main_conf_base_rela_radio").removeClass("main_conf_base_rela_radio_checked");
    $(".main_conf_base_rela_radio[data='" + selected_node.data().relation + "']").addClass("main_conf_base_rela_radio_checked");
    $(".main_conf_param_outer").empty();
    if (selected_node.data().ds) {
        $.get("/api/dataSource/queryByCode", {
            "dsCode": selected_node.data().ds
        },
        function(data) {
            var dsProps = JSON.parse(data.data.dsProps);
            for (var i = 0; i < dsProps.length; i++) {
                var v = selected_node.data().props[dsProps[i].name];
                if (!v) v = "";
                var o = '<div class="param_row"><div class="param_k">' + (dsProps[i].cn_name ? dsProps[i].cn_name: dsProps[i].name) + '</div><div class="param_v"><input type="text" class="param_v_input" value="' + v + '"></div></div>';
                $(".main_conf_param_outer").append(o);
                $(".main_conf_param_outer>.param_row:last").data(dsProps[i]);
            }
        });
        genParams(selected_node.attr("id"));
    }
}

function genParams(id) {
    $.ajax({
        url: "/api/analyseStream/genParams?nodeId=" + id,
        type: "POST",
        async: false,
        contentType: "application/json",
        data: JSON.stringify(getResult()),
        success: function(data) {
            $(".main_conf_aggr_outer").empty();
            if (!data.data) return;
            for (var i = 0; i < data.data.length; i++) {
                var v = selected_node.data().aggrs[data.data[i].name];
                var o = '<div class="param_row"><div class="param_k" style="width:110px">' + (data.data[i].cn_name ? data.data[i].cn_name: data.data[i].name) + '</div><div class="param_v" style="width:114px"><select class="param_v_input" style="width:114px"><option value="none" ' + (!v || v == 'none' ? 'selected': '') + '>不计算</option><option value="count" ' + (v == 'count' ? 'selected': '') + '>count</option><option value="count_distinct" ' + (v == 'count_distinct' ? 'selected': '') + '>count distinct</option><option value="max" ' + (v == 'max' ? 'selected': '') + '>max</option><option value="min" ' + (v == 'min' ? 'selected': '') + '>min</option><option value="avg" ' + (v == 'avg' ? 'selected': '') + '>avg</option><option value="sum" ' + (v == 'sum' ? 'selected': '') + '>sum</option></select></div>';
                $(".main_conf_aggr_outer").append(o);
                $(".main_conf_aggr_outer>.param_row:last").data(data.data[i]);
            }
        }
    });
}

$(document).on("click", ".fellow_node",function(e) {
    $(".main_left_tool>div").removeClass("selected_tool");
    $(".main_left_tool_select").addClass("selected_tool");
    $(".user_node,.fellow_node").removeClass("selected_node");
    $(this).addClass("selected_node");
    selected_node = $(this);
    $(".main_conf_base_type_name").val($(this).children("div:first").text());
    $(".main_conf_base_type_input").val("销售节点");
});

$(".main_conf_base_type_name").keyup(function() {
    selected_node.children("div:first").text($(this).val());
    jsPlumb.repaintEverything();
});

$(".main_tool_var").click(function(){
  $(".main_var,.main_modal").show();
    for(var k in vars){
     var s = '<div class="main_var_table_row">';
         s += '<div class="main_var_table_head_col"  style="width:110px;"> <input type="text" class="main_dynamic_input" value='+k+'></div>';
         s += '<div class="main_var_table_head_col" style="width:110px;"><input type="text" class="main_dynamic_input" value='+vars[k]+'></div>';
         s += '<div class="main_var_table_head_col" style="width:50px;"><div class="main_var_table_head_col_del" style="margin-left:8px;"></div></div> </div>';
     $(".main_var_table_body").append(s);
    }
});

$(".main_var_head_close").click(function(){
  $(".main_var_table_body").empty();
  $(".main_var,.main_modal").hide();
});

var command = false;
$(document).keydown(function(e) {
    if (e.keyCode == 17 || e.keyCode == 91) {
        command = true;
    } else if (command && e.keyCode == 90) {
        undoStack();
    } else if (command && e.keyCode == 8 && selected_node) {
        jsPlumb.remove(selected_node.attr("id"));
        selected_node = null;
    }
});

$(document).keyup(function(e) {
    if (e.keyCode == 17 || e.keyCode == 91) {
        command = false;
    }
});

var node_cnt = 1;
var common = {
    isSource: true,
    isTarget: true
};
jsPlumb.importDefaults({
    PaintStyle: {
        stroke: '#fff',
        strokeWidth: 1
    },
    HoverPaintStyle: {
        stroke: 'red',
        strokeWidth: 2
    },
    Endpoint: "Rectangle",
    Connector: "Straight",
    MaxConnections: -1,
    Overlays: [["Arrow", {
        location: 0,
        foldback: 0.1,
        width: 12,
        length: 12
    }], ["Arrow", {
        location: 1,
        foldback: 1,
        width: 12,
        length: 12
    }]]
});

jsPlumb.bind('click',function(conn) {
    $(".alert_tip").data({
        "type": "main_connect",
        "data": {
            "source": conn.sourceId,
            "target": conn.targetId
        }
    });
    $(".alert_tip,.main_modal").show();
    $(".alert_text").text("确定要删除点击的连接吗？");
});

jsPlumb.bind("beforeDrop",function(info) {
    if (info.sourceId == info.targetId || isConnectionExists(info.sourceId, info.targetId)) {
        return false;
    }
    oper_stack.push({
        "action": "connect",
        "data": {
            "from": info.sourceId,
            "to": info.targetId
        }
    });
    return true;
});

$(".main_tool_undo").click(undoStack);

function undoStack() {
    if (oper_stack.length > 0) {
        var top = oper_stack.pop();
        if (top.action == "connect") {
            jsPlumb.getConnections().forEach(function(connection) {
                if (connection.sourceId == top.data.from && connection.targetId == top.data.to) {
                    jsPlumb.deleteConnection(connection);
                }
            });
        } else if (top.action == "deleteConn") {
            jsPlumb.connect({
                source: top.data.from,
                target: top.data.to,
                maxConnections: -1,
                endpoint: "Blank",
                anchor: top.data.anchor
            });
        } else if (top.action == "appendNode") {
             jsPlumb.remove(top.data);
        } else if (top.action == "removeNode") {
            var o = $("<div id='" + top.data + "' class='" + top.info.type + "_node'><div>" + top.name + "</div><div class='node_result'><div class='node_result_row'></div></div>");
            o.data(JSON.parse(JSON.stringify(top.info)));
            o.css("top", top.top);
            o.css("left", top.left);
            $(".main_canvas_inner_scroll").append(o);
            initNode(top.data);
        } else if (top.action == "drag") {
            $("#" + top.data).css("left", top.pos[0]);
            $("#" + top.data).css("top", top.pos[1]);
            jsPlumb.repaintEverything();
        }
    }
}

function isConnectionExists(sourceId, targetId) {
    var connections = jsPlumb.getConnections();
    for (var i = 0; i < connections.length; i++) {
        var connection = connections[i];
        if (connection.sourceId === sourceId && connection.targetId === targetId || connection.sourceId === targetId && connection.targetId === sourceId) {
            return true;
        }
    }
    return false;
}

$(".main_menu_item_copy").click(function(e) {
    if (!selected_node) return;
    var id;
    if (selected_node.data().type == "user") {
        id = "u_" + node_cnt;
    } else {
        id = "f_" + node_cnt;
    }
    var o = $("<div id='" + id + "' class='" + selected_node.data().type + "_node'><div>" + selected_node.find("div:first").text() + "</div><div class='node_result'></div></div>");
    o.data(JSON.parse(JSON.stringify(selected_node.data())));
    o.css("top", e.clientY - 76 + parseInt($(".main_canvas_inner_scroll").parent().scrollTop()));
    o.css("left", e.clientX - 36 + parseInt($(".main_canvas_inner_scroll").parent().scrollLeft()));
    $(".main_canvas_inner_scroll").append(o);
    focusNode(o);
    oper_stack.push({
        "action": "appendNode",
        "data": id
    });
    initNode(id);
    node_cnt++;
});

$(".main_canvas_inner_scroll").click(function(e) {
    var id, o;
    if (e.target.className == 'main_canvas_inner_scroll' && $(".main_left_tool_fellow").hasClass("selected_tool")) {
        id = "f_" + node_cnt;
        o = $("<div id='" + id + "' class='fellow_node'><div>销售分析节点" + node_cnt + "</div><div class='node_result'></div></div>");
        o.data({
            "type": "fellow",
            "desc": "",
            "relation": "intersection",
            "ds": "",
            "ds_cn_name": "",
            "props": {},
            "aggrs": {}
        });
    } else if (e.target.className == 'main_canvas_inner_scroll' && $(".main_left_tool_user").hasClass("selected_tool")) {
        id = "u_" + node_cnt;
        o = $("<div id='" + id + "' class='user_node'><div>用户分析节点" + node_cnt + "</div><div class='node_result'></div></div>");
        o.data({
            "type": "user",
            "desc": "",
            "relation": "intersection",
            "ds": "",
            "ds_cn_name": "",
            "props": {},
            "aggrs": {}
        });
    }
    if (!id) return;
    o.css("top", e.clientY - 76 + parseInt($(this).parent().scrollTop()));
    o.css("left", e.clientX - 36 + parseInt($(this).parent().scrollLeft()));
    $(this).append(o);
    focusNode(o);
    oper_stack.push({
        "action": "appendNode",
        "data": id
    });
    initNode(id);
    node_cnt++;
});

function initNode(id) {
    jsPlumb.addEndpoint(id, {
        anchor: 'Left',
        endpointStyle: {
            fill: 'transparent',
            height: 20,
            width: 20
        }
    },
    common);
    jsPlumb.addEndpoint(id, {
        anchor: 'Right',
        endpointStyle: {
            fill: 'transparent',
            height: 20,
            width: 20
        }
    },
    common);
    jsPlumb.addEndpoint(id, {
        anchor: 'Top',
        endpointStyle: {
            fill: 'transparent',
            height: 10,
            width: 130
        }
    },
    common);
    jsPlumb.addEndpoint(id, {
        anchor: 'Bottom',
        endpointStyle: {
            fill: 'transparent',
            height: 10,
            width: 130
        }
    },
    common);
    jsPlumb.draggable(id, {
        containment: true,
        start: function(e) {
            oper_stack.push({
                "action": "drag",
                "data": id,
                "pos": [e.el.offsetLeft, e.el.offsetTop]
            });
        }
    });
}

$(".main_conf").height(parseInt($(window).height()) - 66);
$(".main_left_tool,.main_canvas").height(parseInt($(window).height()) - 290);
$(".main_console").width(parseInt($(window).width() - 624));
$(".main_console_area").width(parseInt($(window).width() - 644));
$(".main_canvas").width(parseInt($(window).width() - 294));
$(".main_canvas_inner,.main_chart_inner").width(parseInt($(window).width() - 298));
$(".main_canvas_inner,.main_chart_inner").height(parseInt($(window).height()) - 298);
$(".main_conf_aggr").height(parseInt($(window).height()) - 583);
$(".main_conf_aggr_outer").height(parseInt($(window).height()) - 623);
$(".main_ds").css("left", $(window).width() / 2 - $(".main_ds").width() / 2);
$(".main_ds").css("top", $(window).height() / 2 - $(".main_ds").height() / 2);
$(".main_ds_edit_panel").css("left", $(window).width() / 2 - $(".main_ds_edit_panel").width() / 2);
$(".main_ds_edit_panel").css("top", $(window).height() / 2 - $(".main_ds_edit_panel").height() / 2);
$(".alert_tip").css("left", $(window).width() / 2 - $(".alert_tip").width() / 2);
$(".alert_tip").css("top", $(window).height() / 2 - $(".alert_tip").height() / 2);
$(".main_code").css("left", $(window).width() / 2 - $(".main_code").width() / 2);
$(".main_code").css("top", $(window).height() / 2 - $(".main_code").height() / 2);
$(".main_var").css("left", $(window).width() / 2 - $(".main_var").width() / 2);
$(".main_var").css("top", $(window).height() / 2 - $(".main_var").height() / 2);
$(".main_banner").css("left", $(window).width() / 2 - $(".main_banner").width() / 2);
$(".main_banner").css("top", $(window).height() / 2 - $(".main_banner").height() / 2 - 60);
$(".main_start").css("left", $(window).width() / 2 - $(".main_start").width() / 2);
$(".main_start").css("top", $(window).height() / 2 - $(".main_start").height() / 2 + 200);
$(".main_code_sql").css("left", $(window).width() / 2 - $(".main_code_sql").width() / 2);
$(".main_code_sql").css("top", $(window).height() / 2 - $(".main_code_sql").height() / 2);
$(".copyright_info").css("left", $(window).width() / 2 - $(".copyright_info").width() / 2);

$(window).resize(function() {
    $(".main_left_tool,.main_canvas").height(parseInt($(window).height()) - 290);
    $(".main_conf").height(parseInt($(window).height()) - 66);
    $(".main_console").width(parseInt($(window).width() - 624));
    $(".main_console_area").width(parseInt($(window).width() - 644));
    $(".main_canvas").width(parseInt($(window).width() - 294));
    $(".main_canvas_inner,.main_chart_inner").width(parseInt($(window).width() - 298));
    $(".main_canvas_inner,.main_chart_inner").height(parseInt($(window).height()) - 298);
    $(".main_conf_aggr").height(parseInt($(window).height()) - 583);
    $(".main_conf_aggr_outer").height(parseInt($(window).height()) - 623);
    $(".main_ds").css("left", $(window).width() / 2 - $(".main_ds").width() / 2);
    $(".main_ds").css("top", $(window).height() / 2 - $(".main_ds").height() / 2);
    $(".main_code").css("left", $(window).width() / 2 - $(".main_code").width() / 2);
    $(".main_code").css("top", $(window).height() / 2 - $(".main_code").height() / 2);
    $(".main_code_sql").css("left", $(window).width() / 2 - $(".main_code_sql").width() / 2);
    $(".main_code_sql").css("top", $(window).height() / 2 - $(".main_code_sql").height() / 2);
    $(".main_var").css("left", $(window).width() / 2 - $(".main_var").width() / 2);
    $(".main_var").css("top", $(window).height() / 2 - $(".main_var").height() / 2);
    $(".main_ds_edit_panel").css("left", $(window).width() / 2 - $(".main_ds_edit_panel").width() / 2);
    $(".main_ds_edit_panel").css("top", $(window).height() / 2 - $(".main_ds_edit_panel").height() / 2);
    $(".alert_tip").css("left", $(window).width() / 2 - $(".alert_tip").width() / 2);
    $(".alert_tip").css("top", $(window).height() / 2 - $(".alert_tip").height() / 2);
    $(".main_banner").css("left", $(window).width() / 2 - $(".main_banner").width() / 2);
    $(".main_banner").css("top", $(window).height() / 2 - $(".main_banner").height() / 2 - 60);
    $(".main_start").css("left", $(window).width() / 2 - $(".main_start").width() / 2);
    $(".copyright_info").css("left", $(window).width() / 2 - $(".copyright_info").width() / 2);
    $(".main_start").css("top", $(window).height() / 2 - $(".main_start").height() / 2 + 200);
});

$(".main_banner").fadeIn(1500,function() {
    $(".main_start").fadeIn(500);
});

$(".main_start").click(function() {
    $(".main_start,.main_banner,.copyright_info").hide();
    $(".main_tool,.main_canvas,.main_left_tool,.main_conf,.main_tree,.main_console").show();
});

function formatDateTime(date, format) {
    const o = {
        'M+': date.getMonth() + 1,
        'd+': date.getDate(),
        'h+': date.getHours() % 12 === 0 ? 12 : date.getHours() % 12,
        'H+': date.getHours(),
        'm+': date.getMinutes(),
        's+': date.getSeconds(),
        'q+': Math.floor((date.getMonth() + 3) / 3),
        S: date.getMilliseconds(),
        a: date.getHours() < 12 ? '上午': '下午',
        A: date.getHours() < 12 ? 'AM': 'PM'
    };
    if (/(y+)/.test(format)) {
        format = format.replace(RegExp.$1, (date.getFullYear() + '').substr(4 - RegExp.$1.length));
    }
    for (let k in o) {
        if (new RegExp('(' + k + ')').test(format)) {
            format = format.replace(RegExp.$1, RegExp.$1.length === 1 ? o[k] : ('00' + o[k]).substr(('' + o[k]).length));
        }
    }
    return format;
}

function getCookie(name) {
  const cookies = document.cookie.split(';');
  for (var cookie of cookies) {
    var [cookieName, cookieValue] = cookie.trim().split('=');
    if (cookieName === name) {
      return cookieValue;
    }
  }
  return null;
}

function getColumnNamesFromSQL(sql) {
    const columnPattern = /SELECT\s+([\s\S]*?)\s+FROM\s+/i;
    const match = columnPattern.exec(sql);
    if (match && match[1]) {
        const columnExpressions = match[1].split(/,(?![^\(\)]*\))/g);
        const columnNames = columnExpressions.map(expression =>{
            expression = expression.trim().replace(/\s+/g, ' ');
            const aliasPattern = /\s+AS\s+(["']?)([\w$]+)\1$/i;
            let aliasMatch = aliasPattern.exec(expression);
            if (aliasMatch) {
                return aliasMatch[2];
            } else {
                const simpleExpression = expression.replace(/\(.*?\)/g, '').trim();
                const parts = simpleExpression.split(/\s+/);
                return parts[parts.length - 1];
            }
        });
        return columnNames;
    }
    return [];
}