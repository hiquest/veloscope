package org.veloscope.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.veloscope.exceptions.NoResourceFound;
import org.veloscope.json.Result;
import org.veloscope.resource.EntityInterface;
import org.veloscope.resource.Resource;
import org.veloscope.resource.Scope;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * REST controller. This is the cornerstone of Vello Framework.
 *
 * Actions available:
 *
 * GET /{resource}          for filtered set of objects
 * GET /{resource}/{id}     for getting a single object
 * DELETE /{resource}/{id}  for deleting
 *
 * All these are just regular REST-ful routes.
 *
 */
@Controller
@RequestMapping(value = "/veloapi/{resourceName}")
public class VelloApiController {

    private static final Logger LOG = LoggerFactory.getLogger(VelloApiController.class);

    @Autowired
    private ApplicationContext applicationContext;

    @Transactional
    @RequestMapping(value = { "/{id}", "/{id}/" }, method = { RequestMethod.GET })
    public @ResponseBody
    org.veloscope.json.Result get(@PathVariable String resourceName, @PathVariable Long id) {
        Resource resource = getResource(resourceName);

//        if (!resource.can("get")) { return Result.forbidden(); }

        EntityInterface obj = resource.findById(id);
        if (obj == null) {
            return Result.notFound();
        }

        if (!resource.can("get", obj)) { return Result.forbidden(); }

        return Result.ok(resource.toJSON(obj));
    }

    @Transactional
    @RequestMapping(value = {"/", ""}, method = { RequestMethod.GET })
    public @ResponseBody Result list(@PathVariable String resourceName,
                                     @RequestParam(defaultValue = "0") Integer page,
                                     @RequestParam(defaultValue = "20") Integer perPage,
                                     HttpServletRequest request) {

        List<String> skipParams = Arrays.asList("page", "perPage", "_");
        Resource resource = getResource(resourceName);
        Map<String, String[]> params = request.getParameterMap();

        Scope scope = resource.buildScope();
        for (String param: params.keySet()) {
            if (!skipParams.contains(param)) {
                String p = param.replace('_', '.');
                if (!resource.canFilter(p)) {
                    return Result.forbidden();
                }
                String value = params.get(param)[0];
                scope = scope.by(p, value);
            }
        }

        List out = scope.list(page, perPage);
        return Result.ok(resource.toJSON(out));
    }

//    @Transactional
//    @RequestMapping(value = {"{scope}", "{scope}/"}, method = { RequestMethod.GET })
//    public @ResponseBody Result list(@PathVariable String resourceName,
//                                     @PathVariable String scope,
//                                     @RequestParam(defaultValue = "0") Integer page,
//                                     @RequestParam(defaultValue = "20") Integer perPage,
//                                     HttpServletRequest request) {
//
//        List<String> skipParams = Arrays.asList("page", "perPage");
//        Resource resource = getResource(resourceName);
//
//
//        List out = scope.list(page, perPage);
//        LOG.debug("out: " + out.size());
//        return Result.ok(resource.toJSON(out));
//    }

    @Transactional
    @RequestMapping(value = { "/{id}", "/{id}/" }, method = { RequestMethod.DELETE })
    public @ResponseBody Result<Boolean> delete(@PathVariable String resourceName, @RequestParam Long id) {
        Resource resource = getResource(resourceName);

        EntityInterface obj = resource.findById(id);
        if (obj == null) {
            return Result.notFound();
        }

        if (!resource.canDelete()) {
            return Result.forbidden();
        }

        resource.delete(obj);

        return Result.ok(Boolean.TRUE);
    }

    private Resource getResource(String resourceName) {
        Resource resource;
        try {
            resource = (Resource) applicationContext.getBean(resourceName);
        } catch(NoSuchBeanDefinitionException e) {
            LOG.debug("No such resource found: " + resourceName, e);
            throw new NoResourceFound("No such resource found: " + resourceName);
        }

        return resource;
    }

    // ----------------- Exception handling -------------------
    @ExceptionHandler(NoResourceFound.class)
    public @ResponseBody Result handleIOException() {
        return Result.notFound();
    }

}
