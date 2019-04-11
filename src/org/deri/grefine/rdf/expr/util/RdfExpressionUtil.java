package org.deri.grefine.rdf.expr.util;

import java.io.IOException;
import java.util.Properties;

import org.deri.grefine.rdf.app.ApplicationContext;
import org.deri.grefine.rdf.vocab.VocabularyIndexException;

import com.google.refine.expr.Evaluable;
import com.google.refine.expr.ExpressionUtils;
import com.google.refine.model.Cell;
import com.google.refine.model.Row;

public class RdfExpressionUtil {
        private static ApplicationContext ctx;
    
        public RdfExpressionUtil(ApplicationContext ctx) {
            this.ctx = ctx;
        }
    
	public static Object evaluate(Evaluable eval,Properties bindings,Row row,int rowIndex,String columnName,int cellIndex) throws VocabularyIndexException, IOException{
		Cell cell; 
		if(cellIndex<0){
         	cell= new Cell(rowIndex,null);
         }else{
         	cell= row.getCell(cellIndex);
         }
        ExpressionUtils.bind(bindings, row, rowIndex, columnName, cell, ctx);
		return eval.evaluate(bindings);
	}
}
