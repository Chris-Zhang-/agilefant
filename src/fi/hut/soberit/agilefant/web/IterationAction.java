package fi.hut.soberit.agilefant.web;

import java.text.ParseException;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.opensymphony.xwork2.Action;

import fi.hut.soberit.agilefant.business.IterationBusiness;
import fi.hut.soberit.agilefant.business.ProjectBusiness;
import fi.hut.soberit.agilefant.model.Backlog;
import fi.hut.soberit.agilefant.model.Iteration;
import fi.hut.soberit.agilefant.model.Project;
import fi.hut.soberit.agilefant.transfer.IterationDataContainer;
import fi.hut.soberit.agilefant.transfer.IterationMetrics;
import fi.hut.soberit.agilefant.util.CalendarUtils;

@Component("iterationAction")
@Scope("prototype")
public class IterationAction extends BacklogContentsAction implements CRUDAction {

    private static final long serialVersionUID = -448825368336871703L;

    private int iterationId;

    private Iteration iteration;

    private Project project;

    private int projectId;

    private String startDate;

    private String endDate;

    private String dateFormat;
 
    private IterationMetrics iterationMetrics;
    
    private String json;
    
    @Autowired
    private IterationBusiness iterationBusiness;
    
    @Autowired
    private ProjectBusiness projectBusiness;
    
    private boolean excludeStories = false;
    
    private IterationDataContainer iterationData;
    
    

    public String create() {
        iterationId = 0;
        iteration = new Iteration();
        backlog = iteration;
        
        return Action.SUCCESS;
    }
    
    public String iterationContents() {
        iterationData = this.iterationBusiness.getIterationContents(iterationId);
        if(iterationData == null) {
            return Action.ERROR;
        }
        return Action.SUCCESS;
    }

    public String retrieve() {
        iteration = iterationBusiness.retrieve(iterationId);
        

        if (iteration == null) {
            super.addActionError(super.getText("iteration.notFound"));
            return Action.INPUT;
        }
        
        Date startDate = iteration.getStartDate();
        
        if (startDate == null) {
            startDate = new Date(0);
        }

        project = (Project) iteration.getParent();

        if (project == null) {
            super
                    .addActionError(super
                            .getText("iteration.projectNotFound"));
            return Action.INPUT;
        }
        projectId = project.getId();
        
        // Load metrics data
        iterationMetrics = iterationBusiness.getIterationMetrics(iteration);
//        businessThemeBusiness.loadBacklogThemeMetrics(iteration);
        
        return Action.SUCCESS;
    }
    
    public String delete() {
        iteration = iterationBusiness.retrieve(iterationId);
        iterationBusiness.delete(iterationId);
        return Action.SUCCESS;
    }
    
    public String iterationMetrics() {
        iteration = iterationBusiness.retrieve(iterationId);
        iterationMetrics = iterationBusiness.getIterationMetrics(iteration);
        return Action.SUCCESS;
    }

    public String store() {
        if (iteration == null) {
            super.addActionError(super.getText("iteration.missingForm"));
            return Action.INPUT;
        }
        project = projectBusiness.retrieve(projectId);
        if (project == null) {
            super
                    .addActionError(super
                            .getText("iteration.projectNotFound"));
            return Action.INPUT;
        }
        Iteration fillable = new Iteration();
        if (iterationId > 0) {
            fillable = iterationBusiness.retrieve(iterationId);
            if(projectId > 0 && fillable.getParent() != null 
                    && fillable.getParent().getId() != projectId) {
//                backlogBusiness.removeThemeBindings(fillable);
            }
            if (iteration == null) {
                super.addActionError(super.getText("iteration.notFound"));
                return Action.INPUT;
            }
        }

        try {
            this.fillObject(fillable);
        } catch (ParseException e) {
            super.addActionError(super.getText("backlog.unparseableDate")
                    + super.getText("struts.shortDateTime.format"));
            return Action.ERROR;
        }

        if (super.hasActionErrors()) {
            return Action.ERROR;
        }

        if (iterationId == 0)
            iterationId = (Integer) iterationBusiness.create(fillable);
        else
            iterationBusiness.store(fillable);
        
//        historyBusiness.updateBacklogHistory(fillable.getId());
        return Action.SUCCESS;
    }



    protected void fillObject(Iteration fillable) throws ParseException {
        fillable.setEndDate(CalendarUtils.parseDateFromString(endDate));
        fillable.setStartDate(CalendarUtils.parseDateFromString(startDate));
        if (this.iteration.getName() == null ||
                this.iteration.getName().trim().equals("")) {
            super.addActionError(super.getText("iteration.missingName"));
            return;
        }
        if (fillable.getStartDate().after(fillable.getEndDate())) {
            super
                    .addActionError(super
                            .getText("backlog.startDateAfterEndDate"));
            return;
        }
        fillable.setParent(this.project);
        fillable.setName(this.iteration.getName());
        fillable.setDescription(this.iteration.getDescription());
        fillable.setBacklogSize(this.iteration.getBacklogSize());
        if (fillable.getStartDate().after(fillable.getEndDate())) {
            super
                    .addActionError(super
                            .getText("backlog.startDateAfterEndDate"));
            return;
        }
    }


    public int getIterationId() {
        return iterationId;
    }

    public void setIterationId(int iterationId) {
        this.iterationId = iterationId;
    }

    public Iteration getIteration() {
        return iteration;
    }

    public void setIteration(Iteration iteration) {
        this.iteration = iteration;
        this.backlog = iteration;
    }

    public Backlog getBacklog() {
        return backlog;
    }

    public int getProjectId() {
        return projectId;
    }

    public void setProjectId(int projectId) {
        this.projectId = projectId;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getDateFormat() {
        return dateFormat;
    }

    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
    }

    public String getJsonData() {
        return json;
    }

    public void setIterationBusiness(IterationBusiness iterationBusiness) {
        this.iterationBusiness = iterationBusiness;
    }

    public void setProjectBusiness(ProjectBusiness projectBusiness) {
        this.projectBusiness = projectBusiness;
    }

    public IterationMetrics getIterationMetrics() {
        return iterationMetrics;
    }
    
    public IterationDataContainer getIterationData() {
        return iterationData;
    }

}